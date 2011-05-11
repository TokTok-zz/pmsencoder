@Typed
package com.chocolatey.pmsencoder

class ArgsTest extends PMSEncoderTestCase {
    void testArgsSet() {
        def uri = 'http://args.set.com'
        assertMatch([
            script: '/args_test.groovy',
            uri: uri,
            wantMatches: [ 'Args Set' ],
            hook: [ 'hook', '-foo', '-bar' ],
            wantHook: [ 'hook', '-foo', '-bar', '-baz' ],
            downloader: [ 'downloader', '-foo', '-bar' ],
            wantDownloader: [ 'downloader', '-foo', '-bar', '-baz' ],
            transcoder: [ 'transcoder', '-foo', '-bar' ],
            output: [ '-output', '-foo', '-bar' ],
            wantTranscoder: [ 'transcoder', '-foo', '-bar', '-baz', '-quux', '-output', '-foo', '-bar', '-baz' ]
        ])
    }

    void testArgsRemove() {
        def uri = 'http://args.remove.com'
        assertMatch([
            script: '/args_test.groovy',
            uri: uri,
            wantMatches: [ 'Args Remove' ],
            hook: [ 'hook', '-foo', '-bar' ],
            wantHook: [ 'hook', '-bar' ],
            downloader: [ 'downloader', '-foo', '-bar' ],
            wantDownloader: [ 'downloader', '-bar' ],
            transcoder: [ 'transcoder', '-foo', '-bar' ],
            output: [ '-output', '-foo', '-bar' ],
            wantTranscoder: [ 'transcoder', '-output', '-bar' ]
        ])
    }
}
