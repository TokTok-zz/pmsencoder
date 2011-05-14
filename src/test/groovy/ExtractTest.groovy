@Typed
package com.chocolatey.pmsencoder

class ExtractTest extends PMSEncoderTestCase {
    void testExtract() {
        // due to a bug in groovypp, multiple assignment doesn't work:
        // https://code.google.com/p/groovypptest/issues/detail?id=25
        // we can't even shouldFail it because the exception in the 'conversion' phase
        /*
        shouldFail {
            def (foo, bar, baz) = Util.extract('foo bar baz', '^(\\w+)\\s+(\\w+)\\s+(\\w+)$')
            assert foo == 'foo' && bar == 'bar' && baz == 'baz'
        }
        */
        def list1 = Util.extract('foo bar baz', '^(\\w+)\\s+(\\w+)\\s+(\\w+)$')
        assert list1 == [ 'foo', 'bar', 'baz' ]
        def list2 = Util.extract('foo @@@ baz', '^(\\w+)\\s+(\\w+)\\s+(\\w+)$')
        assert list2 == []
    }
}
