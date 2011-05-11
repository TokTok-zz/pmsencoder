@Typed
package com.chocolatey.pmsencoder

import net.pms.PMS

class PMSEncoderTest extends PMSEncoderTestCase {
    void testResponseCopy() {
        def response = new Response(new Stash([ foo: 'bar' ]), Util.toCommand(TestTranscoder.class, [ 'baz', 'quux' ]))
        assert response != null
        def newResponse = new Response(response)
        assert newResponse != null

        assert !response.stash.is(newResponse.stash)
        assert !response.transcoder.is(newResponse.transcoder)
        assert !response.is(newResponse)
        assert newResponse.stash == response.stash
        assert newResponse.transcoder == response.transcoder
    }

    void testStashCopy() {
        def stash = new Stash([ foo: 'bar' ])
        assert stash != null
        def newStash = new Stash(stash)
        assert newStash != null
        assert !stash.is(newStash)
        assert newStash == new Stash([ foo: 'bar' ])
    }

    void testProfileValidationDelegateInitalState() {
        def delegate = new ProfileValidationDelegate('Test Profile')
        assert delegate != null
        assert delegate.name == 'Test Profile'
        assert delegate.patternBlock == null
        assert delegate.actionBlock == null
    }
}
