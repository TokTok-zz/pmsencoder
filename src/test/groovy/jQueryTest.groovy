@Typed
package com.chocolatey.pmsencoder

class jQueryTest extends PMSEncoderTestCase {
    void setUp() {
        super.setUp()
        def script = this.getClass().getResource('/jquery.groovy')
        matcher.load(script)
    }

    void testjQueryReturn() {
        assertMatch([
            uri: 'http://www.eurogamer.net/videos/uncharted-3-chateau-gameplay-part-2',
            wantMatches: [ 'jQuery' ],
            wantStash: [
                'uri':
                'http://www.eurogamer.net/downloads/80345/uncharted-3-chateau-gameplay-part-2_stream_h264v2_large.mp4'
            ]
        ])
    }
}
