import com.chocolatey.pmsencoder.command.*

import net.pms.PMS

/*
    this is the default/builtin PMSEncoder script. PMSEncoder loads it from
    src/main/resources/scripts/INIT.groovy

    see:

       http://github.com/chocolateboy/pmsencoder/blob/plugin/src/main/resources/scripts/INIT.groovy

    XXX: Don't use this as a tutorial/documentation; see the wiki instead.
    XXX: The scripting framework/DSL is constantly changing, so don't rely on anything here.
*/

init {
    def ncores = PMS.getConfiguration().getNumberOfCpuCores()
    def mplayerLogLevel = 'all=2'

    /*
        Matcher-level (global) lists of strings that are used to initialize the built-in downloaders and
        transcoders

        downloader = new MPlayer()

            mplayer -msglevel all=2 -prefer-ipv4 -quiet -dumpstream -dumpfile DOWNLOADER_OUT URI

        transcoder = new Ffmpeg()

            ffmpeg -v 0 -y -threads ncores -i URI -threads ncores -target ntsc-dvd TRANSCODER_OUT
            ffmpeg -v 0 -y -threads ncores -i DOWNLOADER_OUT -threads ncores -target ntsc-dvd TRANSCODER_OUT

        transcoder = new MEncoder()

            mencoder -mencoder -options -o TRANSCODER_OUT URI
            mencoder -mencoder -options -o TRANSCODER_OUT DOWNLOADER_OUT

        By default, ffmpeg is used without a separate downloader.

        matcher-scoped (i.e. global):

            defaultFfmpegArgs, defaultFfmpegOutputArgs, defaultMencoderArgs,
            and defaultMplayerArgs are lists of strings, but, as seen below, can be assigned strings
            (which are split on whitespace).

        profile-scoped:

            downloader, transcoder and hook are Command objects that can be defined in the context of a profile block.
            It is their job to handle operations on their list of arguments and ultimately to return the
            corresponding command line as a list of strings.
    */

    // default ffmpeg transcode args - all of these defaults can be (p)redefined in a userscript (e.g. BEGIN.groovy)
    // XXX: Groovy quirk: !Ffmpeg.defaultArgs means Ffmpeg.defaultArgs is not a) null or b) empty.
    // these values are all initialized to empty lists, so we're relying on the "is nonempty"
    // meaning for these checks
    if (!Ffmpeg.defaultArgs) {
        // -threads 0 doesn't work for all codecs - better to specify
        Ffmpeg.defaultArgs = "-v 0 -y -threads ${ncores} -i URI"
    }

    // default ffmpeg output options
    if (!Ffmpeg.defaultOutputArgs) {
        Ffmpeg.defaultOutputArgs = "-threads ${ncores} -target ntsc-dvd TRANSCODER_OUT"
    }

    // default mencoder transcode command
    // TODO add support for mencoder-mt
    if (!MEncoder.defaultArgs) {
        MEncoder.defaultArgs = [
            '-msglevel', 'all=2',
            '-quiet',
            '-prefer-ipv4',
            '-cache', '16384', // default cache size; default minimum percentage is 20%
            '-oac', 'lavc',
            '-of', 'lavf',
            '-lavfopts', 'format=dvd',
            '-ovc', 'lavc',
            '-lavcopts', "vcodec=mpeg2video:vbitrate=4096:threads=${ncores}:acodec=ac3:abitrate=128",
            '-ofps', '25',
            '-vf', 'harddup',
            '-o', 'TRANSCODER_OUT',
            'URI'
        ]
    }

    // default mplayer download command
    if (!MPlayer.defaultArgs)
        MPlayer.defaultArgs = "-msglevel ${mplayerLogLevel} -quiet -prefer-ipv4 -dumpstream -dumpfile DOWNLOADER_OUT URI"

    /*
        this is the default list of YouTube format/resolution IDs we should accept/select - in descending
        order of preference.

        it can be modified globally (in a script) to add/remove a format, or can be overridden on
        a per-video basis by supplying a new list to the youtube method (see below) e.g.

        exclude '1080p':

            youtube youtubeAccept - [ 37 ]

        add '3072p':

            youtube([ 38 ] + youtubeAccept)

        For the full list of formats, see: http://en.wikipedia.org/wiki/YouTube#Quality_and_codecs
    */

    if (!youtubeAccept) {
        youtubeAccept = [
            37,  // 1080p
            22,  // 720p
            35,  // 480p
            34,  // 360p
            18,  // Medium
            5    // 240p
        ]
    }
}
