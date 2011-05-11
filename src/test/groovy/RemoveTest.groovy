@Typed
package com.chocolatey.pmsencoder

class RemoveTest extends PMSEncoderTestCase {
    void setUp() {
        super.setUp()
        def script = this.getClass().getResource('/remove.groovy')
        matcher.load(script)
    }

    void testRemoveName() {
        assertMatch([
            uri: 'http://remove.name',
            transcoder: [ 'transcoder', '-foo', '-bar', '-baz', '-quux' ],
            wantMatches: [ 'Remove Name' ],
            wantTranscoder: [ 'transcoder', '-foo', '-baz', '-quux' ]
        ])
    }

    void testRemoveValue() {
        assertMatch([
            uri: 'http://remove.value',
            transcoder: [ 'transcoder', '-foo', '-bar', 'baz', '-quux' ],
            wantMatches: [ 'Remove Value' ],
            wantTranscoder: [ 'transcoder', '-foo', '-quux' ]
        ])
    }

    void testDigitValue() {
        assertMatch([
            uri: 'http://digit.value',
            transcoder: [ 'transcoder', '-foo', '-bar', '-42', '-quux' ],
            wantMatches: [ 'Digit Value' ],
            wantTranscoder: [ 'transcoder', '-foo', '-quux' ]
        ])
    }

    void testHyphenValue() {
        assertMatch([
            uri: 'http://hyphen.value',
            transcoder: [ 'transcoder', '-foo', '-output', '-', '-quux' ],
            wantMatches: [ 'Hyphen Value' ],
            wantTranscoder: [ 'transcoder', '-foo', '-quux' ]
        ])
    }
}
