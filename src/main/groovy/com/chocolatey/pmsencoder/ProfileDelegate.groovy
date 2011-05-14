@Typed
package com.chocolatey.pmsencoder

import org.apache.http.NameValuePair

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/*

    XXX squashed bug: note that delegated methods (i.e. methods exposed via the @Delegate
    annotation) must be *public*:

        All public instance methods present in the type of the annotated field
        and not present in the owner class will be added to owner class
        at compile time.

    http://groovy.codehaus.org/api/groovy/lang/Delegate.html
*/

class ProfileDelegate {
    private final Map<String, String> httpCache = [:]
    private final Map<String, Document> jsoupCache = [:]
    // FIXME: sigh: transitive delegation doesn't work (groovy bug)
    // so make this public so dependent classes can manually delegate to it
    // The order is important! Prefer the local delegate to the global delegate
    @Delegate final Response response
    @Delegate final Matcher matcher

    protected ProfileDelegate(Matcher matcher, Response response) {
        this.matcher = matcher
        this.response = response
    }

    // DSL properties

    // FIXME: use the URI class
    private String getProtocol(String uri) {
        if (uri != null) {
            return RegexHelper.match(uri, '^(\\w+)://')[1]
        } else {
            return null
        }
    }

    // protocol: getter
    public String getProtocol() {
        return getProtocol(response['uri'])
    }

    // DSL getter
    String propertyMissing(String name) {
        // allow a Response-scoped variable to shadow a global (Matcher-0coped) variable
        if (response.stash.containsKey(name)) {
            return response[name]
        } else {
            return matcher[name]
        }
    }

    // DSL setter
    String propertyMissing(String name, Object value) {
        response.let(name, value?.toString())
    }

    // DSL method - can be called from a pattern or an action.
    // actions inherit this method, whereas patterns add the
    // short-circuiting behaviour and delegate to this via super.scrape(...)
    // XXX: we need to declare these two signatures explicitly to work around
    // issues with @Delegate and default parameters

    // curry
    public Function1<Object, Boolean> scrape(Map options) {
        return { Object regex -> scrape(options, regex) }
    }

    public boolean scrape(Object regex) {
        return scrape([ uri: response['uri'] ], regex)
    }

    /*
        1) get the URI pointed to by options['uri'] or response['uri'] (if it hasn't already been retrieved)
        2) perform a regex match against the document
        3) update the stash with any named captures
    */

    public boolean scrape(Map options, Object regex) {
        String uri = (options['uri'] == null) ? response['uri'] : options['uri']
        String document = (options['source'] == null) ? httpCache[uri] : options['source']
        boolean decode = options['decode'] == null ? false : options['decode']

        def newStash = new Stash()
        def scraped = false

        if (document == null) {
            logger.debug("getting $uri")
            assert http != null
            document = httpCache[uri] = http.get(uri)
        }

        if (document == null) {
            logger.error('document not found')
            return scraped
        }

        if (decode) {
            logger.debug("URL-decoding content of $uri")
            document = URLDecoder.decode(document)
        }

        logger.debug("matching content of $uri against $regex")

        if (RegexHelper.match(document, regex, newStash)) {
            logger.debug('success')
            newStash.each { name, value -> response.let(name, value) }
            scraped = true
        } else {
            logger.debug('failure')
        }

        return scraped
    }

    // jsoup.foo() (i.e. property) returns a document preloaded with the current URI
    // jsoup(options) returns a document loaded with the specified URI or string.
    // for greater control, users can import Jsoup and manage it themselves

    // DSL method: curry
    public Function1<Object, Elements> $(Map options) {
        def jsoup

        if (options['source']) {
            jsoup = getJsoupForString(options['source'].toString())
        } else if (options['uri']) {
            jsoup = getJsoupForUri(options['uri'].toString())
        } else {
            jsoup = getJsoupForUri(response['uri'].toString())
        }

        return { Object query -> jsoup.select(query.toString()) }
    }

    // DSL method
    public Document jsoup(Map options) {
        def jsoup

        if (options['source']) {
            jsoup = getJsoupForString(options['source'].toString())
        } else if (options['uri']) {
            jsoup = getJsoupForUri(options['uri'].toString())
        } else {
            jsoup = getJsoupForUri(response['uri'].toString())
        }

        return jsoup
    }

    // DSL method
    public Elements $(Object query) {
        getJsoupForUri(response['uri']).select(query.toString())
    }

    private Document getJsoupForUri(Object obj) {
        def uri = obj.toString()
        def cached = httpCache[uri] ?: (httpCache[uri] = http.get(uri))
        return getJsoupForString(cached)
    }

    private Document getJsoupForString(Object obj) {
        def string = obj.toString()
        return jsoupCache[string] ?: (jsoupCache[string] = Jsoup.parse(string))
    }
}
