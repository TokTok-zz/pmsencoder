@Typed
package com.chocolatey.pmsencoder

class ArgsTest extends PMSEncoderTestCase {
    void testArgsSet() {
        def uri = 'http://args.set.com'
        assertMatch([
            script: '/args.groovy',
            uri: uri,
            wantMatches: [ 'Args', 'Args Set' ],
            wantHook: [ 'hook', '-foo', '-bar', '-baz', '-quux' ],
            downloader: [ 'downloader', '-foo' ],
            wantDownloader: [ 'downloader', '-foo', '-bar' ],
            transcoder: [ 'transcoder', '-foo' ],
            output: [ '-output', '-foo' ],
            wantTranscoder: [ 'transcoder', '-foo', '-bar', '-baz', '-output', '-foo', '-bar' ]
        ])
    }

    void testArgsRemove() {
        def uri = 'http://args.remove.com'
        assertMatch([
            script: '/args.groovy',
            uri: uri,
            wantMatches: [ 'Args', 'Args Remove' ],
            wantHook: [ 'hook', '-foo', '-baz' ],
            downloader: [ 'downloader', '-foo', '-bar', '-baz' ],
            wantDownloader: [ 'downloader', '-foo', '-baz' ],
            transcoder: [ 'transcoder', '-foo', '-bar', '-baz' ],
            output: [ '-output', '-foo', '-bar', '-baz' ],
            wantTranscoder: [ 'transcoder', '-foo', '-output', '-foo', '-baz' ]
        ])
    }
}
