@Typed
package com.chocolatey.pmsencoder

class Pattern {
    // FIXME: sigh: transitive delegation doesn't work (groovy bug)
    // The order is important! Don't delegate to Matcher's propertyMissing
    // before the ProfileDelegate method
    @Delegate private final Response response
    @Delegate private final ProfileDelegate profileDelegate
    @Delegate private final Matcher matcher
    private final Stash stash
    protected static final MatchFailureException STOP_MATCHING = new MatchFailureException()

    Pattern(ProfileDelegate profileDelegate, Stash stash) {
        this.profileDelegate = profileDelegate
        this.stash = stash
        this.matcher = profileDelegate.matcher
        this.response = profileDelegate.response
    }

    String propertyMissing(String name) {
        if (stash.containsKey(name)) {
            stash[name]
        } else {
            profileDelegate.propertyMissing(name)
        }
    }

    String propertyMissing(String name, Object value) {
        stash[name] = value?.toString()
    }

    private String domainToRegex(String domain) {
        def escaped = domain.replaceAll('\\.', '\\\\.')
        return "^https?://(\\w+\\.)*${escaped}(/|\$)".toString()
    }

    // DSL method
    // XXX: test/document this
    protected URI uri(String uri = response['uri']) {
        new URI(uri)
    }

    // DSL method
    protected void domain(Object scalarOrList) {
        String uri = response['uri']
        def matched = Util.scalarList(scalarOrList).any({
            return matchString(uri, domainToRegex(it))
        })

        if (!matched) {
            throw STOP_MATCHING
        }
    }

    // DLS method (alias for domain)
    protected void domains(Object scalarOrList) {
        domain(scalarOrList)
    }

    // DSL method
    protected void protocol(Object scalarOrList) {
        def matched = Util.scalarList(scalarOrList).any({
            return response['uri'].startsWith("${it}://".toString())
        })

        if (!matched) {
            throw STOP_MATCHING
        }
    }

    /*
        We don't have to worry about stash-assignment side-effects, as they're
        only committed if the whole pattern block succeeds. This is handled
        up the call stack (in Profile.match)
    */

    // DSL method
    // XXX squashed bug: avoid infinite loop on scrape by explicitly calling it through profileDelegate
    // XXX: we need to declare these signatures separately to work around issues
    // with @Delegate and default parameters
    protected boolean scrape(Object regex) {
        scrape([:], regex)
    }

    protected boolean scrape(Map options, Object regex) {
        if (profileDelegate.scrape(options, regex)) {
            return true
        } else {
            throw STOP_MATCHING
        }
    }

    // DSL method
    // XXX so much for static typing...
    protected void match(Object object) {
        def matched = true

        if (object instanceof Closure) {
            matched = matchClosure(object as Closure)
        } else if (object instanceof Map) {
            (object as Map<String, Object>).each { name, value ->
                match(response[name], value)
            }
        } else if (object instanceof List) {
            def matches = (object as List)*.toString()
            matched = response.matches.containsAll(matches)
        } else {
            matched = response.matches.contains(object.toString())
        }

        if (!matched) {
            throw STOP_MATCHING
        }
    }

    // DSL method
    // either (String , String) or (String, List)
    protected void match(Object key, Object value) {
        boolean matched // Groovy++ type inference fail (introduced in 0.4.170)

        if (value instanceof List) {
            matched = (value as List).any({ matchString(key?.toString(), it?.toString()) })
        } else {
            matched = matchString(key?.toString(), value?.toString())
        }

        if (!matched) {
            throw STOP_MATCHING
        }
    }

    // DSL method
    private boolean matchClosure(Closure closure) {
        logger.debug('running match block')

        if (closure()) {
            logger.debug('success')
            return true
        } else {
            logger.debug('failure')
            return false
        }
    }

    private boolean matchString(String name, String value) {
        if (name == null) {
            logger.error('invalid match: name is not defined')
        } else if (value == null) {
            logger.error('invalid match: value is not defined')
        } else {
            logger.debug("matching $name against $value")

            if (RegexHelper.match(name, value, response.stash)) {
                logger.debug('success')
                return true // abort default failure below
            } else {
                logger.debug("failure")
            }
        }

        return false
    }
}
