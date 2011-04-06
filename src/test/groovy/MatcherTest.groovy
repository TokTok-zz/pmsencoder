@Typed
package com.chocolatey.pmsencoder

class MatcherTest extends PMSEncoderTestCase {
    // no match - change nothing
    private void noMatch() {
        assertMatch([
            loadDefaultScripts: true,
            uri: 'http://www.example.com',
            wantMatches: []
        ])
    }

    // confirm that there are no side-effects that prevent this returning the same result for the same input
    void testIdempotent() {
        noMatch()
        noMatch()
    }

    void testInterpolationInDefaultTranscoderArgs() {
        assertMatch([
            loadDefaultScripts: true,
            uri: 'http://www.example.com',
            // make sure nbcores is interpolated here as 3 in -threads 3
            // (this is mocked to 3 in PMSEncoderTestCase)
            wantTranscoder: { List<String> transcoder -> transcoder.containsAll([ '-threads', '-3' ]) },
            useDefaultTranscoder: true
        ])
    }

    void testApple() {
        assertMatch([
            loadDefaultScripts: true,
            uri: 'http://www.apple.com/foobar.mov',
            // FIXME: needs to be moved to the downloader
            wantTranscoder: { List<String> transcoder -> transcoder.containsAll([ '-user-agent', 'QuickTime/7.6.2' ]) },
            wantMatches: [ 'Apple Trailers' ]
        ])
    }

    /*
        we can't use assertMatch here due to the volatility of the token (and possibly)
        the highest available resolution.
    */
    void testYouTube() {
        youTubeCommon('35')
    }

    // verify that globally modifying youtubeAccept works
    void testYOUTUBE_ACCEPT() {
        def script = this.getClass().getResource('/youtube_accept.groovy')
        youTubeCommon('34', script)
    }

    private void youTubeCommon(String fmt, URL script = null) {
        def youtube = 'http://www.youtube.com'
        def uri = "$youtube/watch?v=_OBlgSz8sSM"
        assertMatch([
            loadDefaultScripts: true,
            uri: uri,
            script: script,
            wantMatches: ['YouTube Metadata', 'YouTube-DL Compatible', 'YouTube' ],
            wantStash: { Stash stash ->
                assert stash.keySet().toList() == [
                    'uri',
                    'youtube_video_id',
                    'youtube_t',
                    'youtube_title',
                    'youtube_uploader',
                    'youtube_dl_compatible',
                    'youtube_uri',
                    'youtube_fmt'
                ]

                def video_id = stash['youtube_video_id']
                assert video_id == '_OBlgSz8sSM'

                def t = stash['youtube_t']
                // the mysterious t token changes frequently, but always seems to end in a URL-encoded "="
                assert t =~ /.*%3D$/
                assert stash['youtube_uploader'] == 'HDCYT'
                assert stash['youtube_fmt'] == fmt
                assert stash['youtube_uri'] == uri
                assert stash['uri'] =~ '\\.youtube\\.com/videoplayback\\?'
                return true
            },
            wantTranscoder: [ 'transcoder' ]
        ])
    }

    void testGameTrailers() {
        def page_id = '48298'
        def movie_id = '5162'
        def filename = 't_ufc09u_educate_int_gt'
        def uri = "http://www.gametrailers.com/download/$page_id/${filename}.flv"
        def wantURI = "http://trailers-ak.gametrailers.com/gt_vault/${movie_id}/${filename}.flv"

        assertMatch([
            loadDefaultScripts: true,
            uri: uri,
            wantStash: [
                uri:                   wantURI,
                gametrailers_movie_id: movie_id,
                gametrailers_page_id:  page_id,
                gametrailers_filename: filename
            ],
            wantMatches: [ 'GameTrailers' ]
        ])
    }
}
