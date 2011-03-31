// move all of this to GitHub issues

// rename PMSMonkey

// add support for audio

// investigate adding support for images

/* add $mencoder_feature_level (bool) to stash based on MEncoder version */

// get rid of sigils and upper case names

// script management: disable/enable scripts through the Swing UI (cf. GreaseMonkey)

/*
  investigate adding seek support for YouTube videos (e.g. YouTubeEngine):

      http://stackoverflow.com/questions/3302384/youtubes-hd-video-streaming-server-technology
*/

/*

fix the design: http://groovy.codehaus.org/Replace+Inheritance+with+Delegation

Should be:

    Matcher receives an exchange (request/response) object. It creates a
    wrapper that holds methods and members common to Patterns and Actions:

        def exchange = new Exchange(this, request)

    For each Profile, Matcher instantiates a Pattern:

        def pattern = new Pattern(exchange)

    If the pattern matches, it then creates an Action:

        def action = new Action(exchange)

Read-only options could go in request and r/w objects in response e.g.

new Request(
    downloader_out: ...,
    transcoder_out: ...,
    uri: ...,
)

new Response(
    args: ...,
    downloader: ...
    transcoder: ...
    uri: ...
)

matcher.run(request, response)

http://stackoverflow.com/questions/325346/name-for-http-requestresponse
http://stackoverflow.com/questions/1039513/what-is-a-request-response-pair-called

*/

// make $URI a URI rather than a String?

// tests for prepend and append

// migrate (some) regex scrapers to Geb (or Geb + regex)

// when documenting scripting, note poor man's script versioning via Github's "Switch Tags" menu

// a suite of scrapers and extractors:

    scrape:  regex
    browse:  Geb
    query:   Doj and/or port Zombie.js
    xpath:   HtmlUnit?

// use block syntax for scrape?

    scrape { 'foo/bar(?<baz>\\w+)' }
    scrape (uri: uri) { 'foo/bar(?<baz>\\w+)' }

// script metadata?

    script (namespace: 'http://www.example.com', author: 'chocolateboy', version: 1.04) { ... }

// use a web interface because a) Swing sucks and b) headless servers. Only use Swing to enable/disable the web server
// and set the port.

// investigate using busybox-w32/ash instead of cmd.exe on Windows

// Pattern: add extension matcher (use URI):

    extension 'm3u8'
    extension ([ 'mp4', 'm4v' ])

// profile: add $EXTENSION variable

// use URI for protocol parsing rather than a regex

// make action methods (e.g. set, append &c.) methods on the downloader/transcoder e.g.

    def mplayer = new MPlayer()
    transcoder.downloader = mplayer
    downloader.set('-foo')
    transcoder.remove('-bar')

/*

document this:

    script loading order:

        builtin scripts
        user scripts

    script stages:

        begin
        init
        script
        check
        end

    replacing a profile with a profile with a different name
    does not change its canonical name. this ensures other replacement profiles
    have a predictable, consistent name to refer to
*/

/*
    TODO: determine behaviour (if any) if a replacement has a different stage

    TODO: re-stage all the profiles in a script block, preserving the natural order

    keep a list of Script objects rather than (just) a hash of profiles?
*/

// cleaner Gradle-style names (no sigils!):

class MyTranscoder extends Transcoder {
    List<String> toCommandLine() {

    }
}

profile('Foo') {
    pattern {
        match { uri == 'http://whatever' } // TODO: make uri an actual URI rather than a string
    }

    action {
        // default
        transcoder = new FFmpeg() // assigns default args
        transcoder.args = "string -or -list"
        transcoder.output = "string -or -list" // ffmpeg only: output options

        // add a downloader
        def mplayer = new MPlayer(uri)
        mplayer.args = [ 'string', '-or', '-list' ]
        transcoder.downloader = mplayer
    }
}

// No need to expose $PMS. Just use PMS.get() as normal

// store the original URI: e.g. for mplayer.groovy

if (originalUri.protocol == 'concat') { ... }

// bring back reject: e.g. for mplayer.groovy:

reject $URI: '^concat:'

// add a commit method which stops all further profile matching for this request

/*

    image to video:

        ffmpeg -r 24 -i http://ip:port/plugin/name/imdb_plot?id=42&fmt=png \
            -vcodec mpeg2video -qscale 2 -vframes 1 transcoder.out

*/

/*

Transcoders:

    MEncoder
    FFmpeg

Downloaders:

    MPlayer
    CustomDownloader // e.g. get-flash-videos, youtube-dl
*/

// Ruby-style initialization blocks?

profile ('Whatever') {
    transcoder = new FFmpegTranscoder() {
        downloader = new CustomDownloader() {
            executable = '/path/to/mydownloader'
            args = "-referrer $referrer -o $downloaderOut -i $uri"
        }
    }
    hook = "foo bar baz"
}

// add a navix:// protocol e.g. navix://default?referrer=url_encoded_uri&url=...

// need to be more precise/verbose with the names e.g. MPlayer could be used as a "null"/identity transcoder
// (-oac copy -ovc copy):

transcoder = new NullTranscoder()

// need to pass in the renderer

// test Pattern.scrape

/// FIXME: MPlayer can't dump to stdout: http://lists.mplayerhq.hu/pipermail/mplayer-users/2006-April/059898.html

// need better vlc detection

// make the rtmp2pms functionality available via a web page (e.g. GitHub page) using JavaScript:
// i.e. enter 1) name/path 2) the command line 3) optional thumbnail URI and click to generate the WEB.conf
// line

// Env.js + jQuery + Rhino:

/*
    var stage = new Stage('script');
    var profile = new Profile('Foo', [ 'extends': 'Bar', 'replaces': 'Baz' ]);
    stage.addProfile(profile);

    profile.pattern = function(context, uri) {
        if (uri.match(/^http:\/\/www.whatever.com/) {
            return true;
        }
        return false
    };

    profile.action = ...
*/

// spock-esque?

    pattern:

        if (uri.whatever()) {
            return true;
        } else {
            return false;
        }

    action:

        uri = $(...)

// propertyMissing + methodMissing?

    uri = jQuery { $(...).foo().bar().baz() }

// complement (asynchronous) $HOOK with $BEFORE and $AFTER. $AFTER attaches a dummy process started by stopProcess()

    script {
        // .js files use common.js exports object to export jsPattern and jsAction
        // e.g. exports['jsPattern'] = function(uri) { ... };
        // methods looked up in map via methodMissing?
        jsLoadResource('jspattern.js')
        jsLoadFile('jsaction.js')

        pattern {
            match { jsPattern(uri) }
        }

        action {
            transcoder = jsAction(uri)
        }
    }
