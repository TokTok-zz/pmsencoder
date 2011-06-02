@Typed
package com.chocolatey.pmsencoder

class PMSConfTest extends PMSEncoderTestCase {
    void testPMSConf() {
        assertMatch([
            // needed for the URI substitution in toList()
            uri: 'http://example.com',
            script: '/pmsconf.groovy',
            wantMatches: [ 'pmsConf' ],
            wantTranscoder: [ 'transcoder', '-rtmpdump-path', '/usr/bin/rtmpdump' ]
        ])
    }
}
