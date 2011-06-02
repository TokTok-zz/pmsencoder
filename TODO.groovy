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

// script metadata?

    script (namespace: 'http://www.example.com', author: 'chocolateboy', version: 1.04) { ... }

// use a web interface because a) Swing sucks and b) headless servers. Only use Swing to enable/disable the web server
// and set the port.

// Pattern: add extension matcher (use URI):

    extension 'm3u8'
    extension ([ 'mp4', 'm4v' ])

// profile: add extension variable

    pattern {
        match { extension == 'asx' }
    }

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

Future?

    MPlayer2

Downloaders:

    MPlayer
    RTMPDump
    CustomDownloader // e.g. get-flash-videos, youtube-dl

*/

// add a navix:// protocol e.g. navix://navix?referrer=url_encoded_uri&url=...

// need better vlc detection

// make the rtmp2pms functionality available via a web page (e.g. GitHub page) using JavaScript:
// i.e. enter 1) name/path 2) the command line 3) optional thumbnail URI and click to generate the WEB.conf
// line

// complement (asynchronous) hook with before and after. after attaches a dummy process started by stopProcess()

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

// XXX: TODON'T: we can't get rid of the top-level stage method (i.e. a script is just zero or more profiles)
// as it would break BEGIN.groovy, which needs to establish its stage (begin) but doesn't define a profile.
// similarly, don't hoist engine up into a stage parameter as it's profile-specific and would be nonsensical
// in BEGIN.groovy

// move commands to their own namespace e.g. com.chocolatey.pmsencoder.commands.MPlayer
