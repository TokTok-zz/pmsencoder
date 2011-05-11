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
            logger.trace('no pattern block supplied: matched OK')
        } else {
            // pattern methods short-circuit matching on failure by throwing a MatchFailureException,
            // so we need to wrap this in a try/catch block

            try {
                patternBlock.delegate = pattern
                patternBlock.resolveStrategy = Closure.DELEGATE_ONLY
                patternBlock()
            } catch (MatchFailureException e) {
                logger.trace('pattern block: caught match exception')
                // one of the match methods failed, so the whole block failed
                logger.debug("match $name: failure")
                return false
            }

            // success simply means "no match failure exception was thrown" - this also handles cases where the
            // pattern block is empty
            logger.trace('pattern block: matched OK')
        }

        logger.debug("match $name: success")
        return true
    }

    // pulled out of the match method below so that type-softening is isolated
    // note: keep it here rather than making it a method in Action: trying to keep the delegates
    // clean (cf. Ruby's BlankSlate)
    boolean runActionBlock(ProfileDelegate profileDelegate) {
        if (actionBlock != null) {
            def action = new Action(profileDelegate)
            logger.trace("running action block for: $name")
            actionBlock.delegate = action
            actionBlock.resolveStrategy = Closure.DELEGATE_ONLY
            actionBlock()
            logger.trace("finished action block for: $name")
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
            logger.trace('no pattern block supplied: matched OK')
            runActionBlock(profileDelegate)
            // fall through to return true
        } else {
            // pass in a stash so that a) the pattern doesn't contaminate the response's
            // stash with incomplete/aborted assignments in the event of a failed match
            // and b) the pattern can suppress logging. in the latter case we log all the assignments
            // *after* a successful match when the pattern's stash is merged into the response stash
            def patternStash = new Stash()
            def pattern = new Pattern(profileDelegate, patternStash)

            logger.debug("matching profile: $name")

            // returns true if all matches in the block succeed, false otherwise
            if (runPatternBlock(pattern)) {
                // we can now merge any side-effects - i.e. modifications to the stash - and log them.
                patternStash.each { name, value -> response.let(name, value) }
                // now run the action
                runActionBlock(profileDelegate)
                // fall through to return true
            } else {
                return false
            }
        }

        return true
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
