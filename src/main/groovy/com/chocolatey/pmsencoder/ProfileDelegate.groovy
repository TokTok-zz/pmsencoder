@Typed
package com.chocolatey.pmsencoder

import org.apache.http.NameValuePair

/*

    XXX squashed bug: note that delegated methods (i.e. methods exposed via the @Delegate
    annotation) must be *public*:

        All public instance methods present in the type of the annotated field
        and not present in the owner class will be added to owner class
        at compile time.

    http://groovy.codehaus.org/api/groovy/lang/Delegate.html
*/

class ProfileDelegate {
    private final Map<String, String> cache = [:] // only needed/used by this.scrape()
    @Lazy private Browser browser = new Browser()
    // FIXME: sigh: transitive delegation doesn't work (groovy bug)
    // so make this public so dependent classes can manually delegate to it
    // The order is important! Prefer the local delegate to the global delegate
    @Delegate final Response response
    @Delegate final Matcher matcher

    public ProfileDelegate(Matcher matcher, Response response) {
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
        String document = (options['source'] == null) ? cache[uri] : options['source']
        boolean decode = options['decode'] == null ? false : options['decode']

        def newStash = new Stash()
        def scraped = false

        if (document == null) {
            logger.debug("getting $uri")
            assert http != null
            document = cache[uri] = http.get(uri)
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

    // curry
    Function1<Object, String> jQuery(Map options) {
        return { Object query -> jQuery(String.class, options, query) }
    }

    String jQuery(Object query) {
        jQuery(String.class, [ uri: response['uri'] ], query)
    }

    public <T> T jQuery(Class<T> klass, Object query) {
        return jQuery(klass, [ uri: response['uri'] ], query)
    }

    public <T> Function1<Object, T> jQuery(Class<T> klass, Map options) {
        return { Object query -> jQuery(klass, options, query) }
    }

    public <T> T jQuery(Class<T> klass, Map options, Object query) {
        String uri = (options['uri'] == null) ? response['uri'] : options['uri']
        // String document = (options['source'] == null) ? cache[uri] : options['source']
        browser.navigate(uri)
        return browser.eval(klass, query.toString())
    }
}
