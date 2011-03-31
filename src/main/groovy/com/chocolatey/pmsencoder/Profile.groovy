@Typed
package com.chocolatey.pmsencoder

import org.apache.log4j.Level

// this holds a reference to the pattern and action blocks, and isn't delegated to
class Profile implements LoggerMixin {
    private final Matcher matcher
    private Closure patternBlock
    private Closure actionBlock
    final Stage stage
    final String name

    Profile(Matcher matcher, String name, Stage stage) {
        this.matcher = matcher
        this.name = name
        this.stage = stage
    }

    void extractBlocks(Closure closure) {
        def delegate = new ProfileValidationDelegate(name)
        // wrapper method: runs the closure then validates the result, raising an exception if anything is amiss
        delegate.runProfileBlock(closure)

        // we made it without triggering an exception, so the two fields are sane: save them
        this.patternBlock = delegate.patternBlock // possibly null
        this.actionBlock = delegate.actionBlock   // possibly null
    }

    // pulled out of the match method below so that type-softening is isolated
    // note: keep it here rather than making it a method in Pattern: trying to keep the delegates
    // clean (cf. Ruby's BlankSlate)
    boolean runPatternBlock(Pattern pattern) {
        if (patternBlock == null) {
            // unconditionally match
            log.trace('no pattern block supplied: matched OK')
        } else {
            // pattern methods short-circuit matching on failure by throwing a MatchFailureException,
            // so we need to wrap this in a try/catch block

            try {
                patternBlock.delegate = pattern
                patternBlock.resolveStrategy = Closure.DELEGATE_FIRST
                patternBlock()
            } catch (MatchFailureException e) {
                log.trace('pattern block: caught match exception')
                // one of the match methods failed, so the whole block failed
                log.debug("match $name: failure")
                return false
            }

            // success simply means "no match failure exception was thrown" - this also handles cases where the
            // pattern block is empty
            log.trace('pattern block: matched OK')
        }

        log.debug("match $name: success")
        return true
    }

    // pulled out of the match method below so that type-softening is isolated
    // note: keep it here rather than making it a method in Action: trying to keep the delegates
    // clean (cf. Ruby's BlankSlate)
    boolean runActionBlock(ProfileDelegate profileDelegate) {
        if (actionBlock != null) {
            def action = new Action(profileDelegate)
            log.trace("running action block for: $name")
            actionBlock.delegate = action
            actionBlock.resolveStrategy = Closure.DELEGATE_FIRST
            actionBlock()
            log.trace("finished action block for: $name")
            return true
        } else {
            return false
        }
    }

    boolean match(Response response) {
        if (patternBlock == null && actionBlock == null) {
            return true
        }

        def profileDelegate = new ProfileDelegate(matcher, response)

        if (patternBlock == null) { // unconditionally match
            log.trace('no pattern block supplied: matched OK')
            runActionBlock(profileDelegate)
            return true
        } else {
            def newResponse = response.clone()
            def patternProfileDelegate = new ProfileDelegate(matcher, newResponse)

            // avoid clogging up the logfile with pattern-block stash assignments. If the pattern doesn't match,
            // the assigments are irrelevant; and if it does match, the assignments are logged (below)
            // when the pattern's temporary stash is merged into the response stash. Rather than suppressing these
            // assignments completely, log them at the lowest (TRACE) level
            newResponse.setStashAssignmentLogLevel(Level.TRACE)

            // the pattern block has its own response object (which is initially the same as the action block's).
            // if the match succeeds, then the pattern block's stash is merged into the action block's stash.
            // this ensures that a partial match (i.e. a failed match) with side-effects/bindings doesn't contaminate
            // the action, and, more importantly, it defers logging until the whole pattern block has
            // completed successfully
            def pattern = new Pattern(patternProfileDelegate)

            log.debug("matching profile: $name")

            // returns true if all matches in the block succeed, false otherwise
            if (runPatternBlock(pattern)) {
                // we can now merge any side-effects (currently only modifications to the stash).
                // first: merge (with logging)
                newResponse.stash.each { name, value -> response.let(name, value) }
                // now run the actions
                runActionBlock(profileDelegate)
                return true
            } else {
                return false
            }
        }
    }

    public void assignPatternBlockIfNull(Profile profile) {
        // XXX where is ?= ?
        if (this.patternBlock == null) {
            this.patternBlock = profile.patternBlock
        }
    }

    public void assignActionBlockIfNull(Profile profile) {
        // XXX where is ?= ?
        if (this.actionBlock == null) {
            this.actionBlock = profile.actionBlock
        }
    }
}
