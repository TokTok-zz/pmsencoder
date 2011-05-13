// move all of this to GitHub issues

// rename PMSMonkey?

// add support for audio

// investigate adding support for images

// script management: disable/enable scripts through the Swing UI (cf. GreaseMonkey)

/*
  investigate adding seek support for YouTube videos (e.g. YouTubeEngine):

      http://stackoverflow.com/questions/3302384/youtubes-hd-video-streaming-server-technology
*/

// test coverage

// migrate (some) regex scrapers to Geb (or Geb + regex)

// a suite of scrapers and extractors:

    scrape:  regex
    browse:  Geb
    query:   Doj and/or port Env.js
    xpath:   HtmlUnit?

// use block syntax for scrape?

    scrape { 'foo/bar(?<baz>\\w+)' }
    scrape (uri: uri) { 'foo/bar(?<baz>\\w+)' }

// script metadata?

    script (namespace: 'http://www.example.com', author: 'chocolateboy', version: 1.04) { ... }

// use a web interface because a) Swing sucks and b) headless servers. Only use Swing to enable/disable the web server
// and set the port.

// Pattern: add extension matcher (use URI):

    extension 'm3u8'
    extension ([ 'mp4', 'm4v' ])

// profile: add extension variable

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

    replacing a profile with another profile with a different name
    does not change its canonical name. this ensures other replacement profiles
    have a predictable, consistent name to refer to
*/

/*
    TODO: determine behaviour (if any) if a replacement has a different stage

    TODO: re-stage all profiles in a stage block, preserving the natural order

    keep a list of Script objects rather than (just) a hash of profiles?
*/

// No need to expose pms? Just use PMS.get() as normal

// bring back reject: e.g. for mplayer.groovy:

reject uri: '^concat:'

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
    MPlayer2

Downloaders:

    MPlayer
    RTMPDump
    CustomDownloader // e.g. get-flash-videos, youtube-dl

*/

// add a navix:// protocol e.g. navix://default?referrer=url_encoded_uri&url=...

// need better vlc detection

// make the rtmp2pms functionality available via a web page (e.g. GitHub page) using JavaScript:
// i.e. enter 1) name/path 2) the command line 3) optional thumbnail URI and click to generate the WEB.conf
// line

// Env.js + jQuery + Rhino: http://snipplr.com/view/38607/rhino-envjs-testing-example/

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
// http://groovy.dzone.com/articles/groovy-action-statically-typed

    uri = jQuery { $(...).foo().bar().baz() }

// complement (asynchronous) hook with before and after. after attaches a dummy process started by stopProcess()

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

// pass renderer configuration in request

// PMS: call player.mimeType(rendererConf) rather than player.mimeType() - wire the former to the latter in Engine.java

// use slf4j/Logback and use the same debugLogPath as PMS

// test/document new uri() method e.g.

    match { uri().host == 'www.example.com' }
    match { uri(uri).port == 8080 }

// groovy at its grooviest: http://groovy.codehaus.org/Process+Management

    def process = "seq 10 99".execute() | "tr 0123456789 9876543210".execute()
    print process.text

// implicit imports: automatically import e.g. Ffmpeg &c. into scripts:
// TODO: see how gradle does it
// http://groovy.329449.n5.nabble.com/Implicit-imports-for-scripts-td355430.html
