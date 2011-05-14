@Typed
package com.chocolatey.pmsencoder

class JsoupTest extends PMSEncoderTestCase {
    void setUp() {
        super.setUp()
        def script = this.getClass().getResource('/jsoup_test.groovy')
        matcher.load(script)
    }

    void testJsoup() {
        assertMatch([
            uri: 'http://www.eurogamer.net/videos/uncharted-3-chateau-gameplay-part-2',
            wantMatches: [ 'jsoup' ],
            wantStash: [
                'uri':
                'http://www.eurogamer.net/downloads/80345/uncharted-3-chateau-gameplay-part-2_stream_h264v2_large.mp4'
            ]
        ])
    }
}
