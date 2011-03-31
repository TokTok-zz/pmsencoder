@Typed
package com.chocolatey.pmsencoder

import static com.chocolatey.pmsencoder.Util.quoteURI

import net.pms.configuration.PmsConfiguration
import net.pms.dlna.DLNAMediaInfo
import net.pms.encoders.MEncoderWebVideo
import net.pms.io.OutputParams
import net.pms.io.ProcessWrapper
import net.pms.PMS

class PMSEncoder extends MEncoderWebVideo implements LoggerMixin {
    public static final boolean isWindows = PMS.get().isWindows()
    private Plugin plugin
    private final static ThreadLocal threadLocal = new ThreadLocal<String>()
    private static final String DEFAULT_MIME_TYPE = 'video/mpeg'

    final PmsConfiguration configuration
    public static final String ID = 'pmsencoder'

    private long currentThreadId() {
        Thread.currentThread().getId()
    }

    @Override
    public String mimeType() {
        def mimeType = threadLocal.get()

        if (mimeType != null) { // transcode thread
            log.debug('thread id: ' + currentThreadId())
            log.info("getting custom mime type: $mimeType")
            threadLocal.remove() // remove it to prevent memory leaks
            return mimeType
        } else {
            return DEFAULT_MIME_TYPE
        }
    }

    @Override
    public String name() {
        'PMSEncoder'
    }

    private String normalizePath(String path) {
        return isWindows ? path.replaceAll(~'/', '\\\\') : path
    }

    @Override
    public String executable() {
        normalizePath(super.executable())
    }

    @Override
    public String id() {
        ID
    }

    public PMSEncoder(PmsConfiguration configuration, Plugin plugin) {
        super(configuration)
        this.configuration = configuration
        this.plugin = plugin
    }

    @Override
    public ProcessWrapper launchTranscode(String oldURI, DLNAMediaInfo media, OutputParams params)
    throws IOException {
        def processManager = new ProcessManager(this, params)
        def threadId = currentThreadId() // make sure concurrent threads don't use the same filename
        def uniqueId = System.currentTimeMillis() + '_' + threadId
        def transcoderOutputBasename = "pmsencoder_transcoder_out_${uniqueId}" // always used (read by PMS)
        def transcoderOutputPath = processManager.getFifoPath(transcoderOutputBasename)
        def downloaderOutputBasename = "pmsencoder_downloader_out_${uniqueId}"
        def downloaderOutputPath = isWindows ? '-' : processManager.getFifoPath(downloaderOutputBasename)

        // whatever happens, we need a transcoder output FIFO (even if there's a match error, we carry
        // on with the unmodified URI), so we can create that upfront
        processManager.createTranscoderFifo(transcoderOutputBasename)

        def ffmpegPath = normalizePath(configuration.getFfmpegPath())
        def mencoderPath = normalizePath(configuration.getMencoderPath())
        def mencoderMtPath = normalizePath(configuration.getMencoderMTPath())
        def mplayerPath = normalizePath(configuration.getMplayerPath())

        def request = new Request()
        request.params = params
        request.uri = oldURI

        /*
        oldStash.put('$DOWNLOADER_OUT', downloaderOutputPath)
        oldStash.put('$TRANSCODER_OUT', transcoderOutputPath)
        */

        log.info('invoking matcher for: ' + oldURI)

        def response = plugin.match(request)
        def matches = response.matches
        def nMatches = matches.size()

        if (nMatches == 0) {
            log.info('0 matches for: ' + oldURI)
        } else if (nMatches == 1) {
            log.info('1 match (' + matches + ') for: ' + oldURI)
        } else {
            log.info(nMatches + ' matches (' + matches + ') for: ' + oldURI)
        }

        def mimeType = response.stash.get('$MIME_TYPE')

        if (mimeType != null) {
            log.debug('thread id: ' + threadId)
            log.info("setting custom mime-type: $mimeType")
            threadLocal.set(mimeType)
        } else {
            threadLocal.remove() // remove it to prevent memory leaks
        }

        // FIXME: groovy++ type inference fail: the subscript and/or concatenation operations
        // on downloaderArgs and transcoderArgs are causing groovy++ to define them as
        // Collection<String> rather than List<String>
        List<String> hookArgs = response.hook
        List<String> downloaderArgs = response.downloader
        List<String> transcoderArgs = response.transcoder
        def newURI = quoteURI(response.stash.get('$URI'))

        if (hookArgs) {
            processManager.handleHook(hookArgs)
        }

        // automagically add extra command-line options for the PMS-native downloaders/transformers
        // and substitute the configured paths for 'MPLAYER', 'FFMPEG' &c.
        // TODO: add support for GET_FLASH_VIDEOS, YOUTUBE_DL and RTMPDUMP "macros" (any others?)
        if (downloaderArgs) {
            downloaderArgs.replaceAll('DOWNLOADER_OUT', downloaderOutputPath)
            if (downloaderArgs[0] == 'MPLAYER') {
                /*
                    plug in the input/output e.g. before:

                        mplayer -prefer-ipv4 -quiet -dumpstream

                    after:

                        /path/to/mplayer -prefer-ipv4 -quiet -dumpstream -dumpfile $DOWNLOADER_OUT $URI
                 */

                downloaderArgs[0] = mplayerPath
                downloaderArgs += [ '-dumpfile', downloaderOutputPath, newURI ]
            }
        }

        if (transcoderArgs) {
            transcoderArgs.replaceAll('TRANSCODER_OUT', transcoderOutputPath)
            def transcoder = transcoderArgs[0]

            if (transcoder != null && transcoder in [ 'FFMPEG', 'MENCODER', 'MENCODER_MT' ]) {
                def transcoderInput = downloaderArgs ? downloaderOutputPath : newURI

                if (transcoder == 'FFMPEG') {
                    /*
                        before:

                             ffmpeg -v 0 -y -threads nbcores

                        after (with downloader):

                             /path/to/ffmpeg -v 0 -y -threads nbcores -i $DOWNLOADER_OUT \
                                -threads nbcores -target ntsc-dvd $TRANSCODER_OUT

                        after (without downloader):

                             /path/to/ffmpeg -v 0 -y -threads nbcores -i $URI -threads nbcores \
                                 -target ntsc-dvd $TRANSCODER_OUT
                    */

                    transcoderArgs[0] = ffmpegPath
                    transcoderArgs += [ '-i', transcoderInput ]
                    if (response.output) {
                        transcoderArgs += response.output // defaults to: -target ntsc-dvd
                    }
                    transcoderArgs += [ transcoderOutputPath ]
                } else { // mencoder
                    /*
                        before:

                             mencoder -mencoder -options

                        after (with downloader):

                             /path/to/mencoder -mencoder -options -o $TRANSCODER_OUT $DOWNLOADER_OUT

                        after (without downloader):

                             /path/to/mencoder -mencoder -options -o $TRANSCODER_OUT $URI
                    */

                    transcoderArgs[0] = transcoder == 'MENCODER' ? mencoderPath : mencoderMtPath
                    transcoderArgs += [ '-o', transcoderOutputPath, transcoderInput ]
                }
            }
        }

        // Groovy's "documentation" doesn't make it clear whether local variables are null-initialized
        // http://stackoverflow.com/questions/4025222
        def transcoderProcess = null

        if (downloaderArgs) {
            if (isWindows) {
                transcoderProcess = processManager.handleDownloadWindows(downloaderArgs, transcoderArgs)
            } else {
                processManager.handleDownloadUnix(downloaderArgs, downloaderOutputBasename)
            }
        }

        if (transcoderProcess == null) {
            transcoderProcess = processManager.handleTranscode(transcoderArgs)
        }

        return processManager.launchTranscode(transcoderProcess)
    }
}
