@Typed
package com.chocolatey.pmsencoder

import net.pms.PMS

class PMSEncoderTest extends PMSEncoderTestCase {
    void testResponseClone() {
        def response = new Response([ foo: 'bar' ], [ 'baz', 'quux' ])
        assert response != null
        def newResponse = response.clone()
        assert newResponse != null

        assert !response.stash.is(newResponse.stash)
        assert !response.transcoder.is(newResponse.transcoder)
        assert !response.is(newResponse)
        assert newResponse.stash == [ $foo: 'bar' ]
        assert newResponse.transcoder == [ 'baz', 'quux' ]
    }

    void testResponseCopy() {
        def response = new Response([ foo: 'bar' ], [ 'baz', 'quux' ])
        assert response != null
        def newResponse = new Response(response)
        assert newResponse != null

        assert !response.stash.is(newResponse.stash)
        assert !response.transcoder.is(newResponse.transcoder)
        assert !response.is(newResponse)
        assert newResponse.stash == [ $foo: 'bar' ]
        assert newResponse.transcoder == [ 'baz', 'quux' ]
    }

    void testStashClone() {
        def stash = new Stash([ foo: 'bar' ])
        assert stash != null
        def newStash = new Stash(stash)
        assert newStash != null
        assert !stash.is(newStash)
        assert newStash == [ $foo: 'bar' ]
    }

    void testProfileValidationDelegateInitalState() {
        def delegate = new ProfileValidationDelegate('Test Profile')
        assert delegate != null
        assert delegate.name == 'Test Profile'
        assert delegate.patternBlock == null
        assert delegate.actionBlock == null
    }
}
