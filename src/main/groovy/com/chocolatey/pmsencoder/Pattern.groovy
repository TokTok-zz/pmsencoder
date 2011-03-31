@Typed
package com.chocolatey.pmsencoder

class Pattern {
    @Delegate private ProfileDelegate profileDelegate
    protected static final MatchFailureException STOP_MATCHING = new MatchFailureException()
    // FIXME: sigh: transitive delegation doesn't work (groovy bug)
    @Delegate private final Matcher matcher

    Pattern(ProfileDelegate profileDelegate) {
        this.profileDelegate = profileDelegate
        this.matcher = profileDelegate.matcher
    }

    // DSL setter - overrides the ProfileDelegate method to avoid logging,
    // which is handled later (if the match succeeds) by merging the pattern
    // block's temporary stash
    protected String propertyMissing(String name, Object value) {
        response.setVar(name, value)
    }

    protected String propertyMissing(String name) {
        profileDelegate.propertyMissing(name)
    }

    // DSL method
    protected void domain(Object scalarOrList) {
        def uri = response.getVar('$URI')
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
            return response.getVar('$URI').startsWith("${it}://".toString())
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
        scrape(regex, [:])
    }

    protected boolean scrape(Object regex, Map options) {
        if (profileDelegate.scrape(regex, options)) {
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
            (object as Map).each { name, value ->
                match(response.getVar(name), value)
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
            matched = (value as List).any({ matchString(key, it) })
        } else {
            matched = matchString(key, value)
        }

        if (!matched) {
            throw STOP_MATCHING
        }
    }

    // DSL method
    private boolean matchClosure(Closure closure) {
        log.debug('running match block')

        if (closure()) {
            log.debug('success')
            return true
        } else {
            log.debug('failure')
            return false
        }
    }

    private boolean matchString(Object name, Object value) {
        if (name == null) {
            log.error('invalid match: name is not defined')
        } else if (value == null) {
            log.error('invalid match: value is not defined')
        } else {
            log.debug("matching $name against $value")

            if (RegexHelper.match(name, value, response.stash)) {
                log.debug('success')
                return true // abort default failure below
            } else {
                log.debug("failure")
            }
        }

        return false
    }

    // DSL method
    protected String domainToRegex(Object domain) {
        def escaped = domain.toString().replaceAll('\\.', '\\\\.')
        return "^https?://(\\w+\\.)*${escaped}(/|\$)".toString()
    }
}
