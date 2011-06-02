@Typed
package com.chocolatey.pmsencoder

import static com.chocolatey.pmsencoder.Util.quoteURI

import net.pms.configuration.PmsConfiguration
import net.pms.dlna.DLNAMediaInfo
import net.pms.dlna.DLNAResource
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
            logger.debug('thread id: ' + currentThreadId())
            logger.info("getting custom mime type: ${mimeType}")
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
    public ProcessWrapper launchTranscode(
        String oldURI,
        DLNAResource dlna,
        DLNAMediaInfo media,
        OutputParams params)
    throws IOException {
        def processManager = new ProcessManager(this, params)
        def threadId = currentThreadId() // make sure concurrent threads don't use the same filename
        def uniqueId = System.currentTimeMillis() + '_' + threadId
        def transcoderOutputBasename = "pmsencoder_transcoder_out_${uniqueId}" // always used (read by PMS)
        def transcoderOutputPath = processManager.getFifoPath(transcoderOutputBasename)
        def downloaderOutputBasename = "pmsencoder_downloader_out_${uniqueId}"
        def downloaderOutputPath = processManager.getFifoPath(downloaderOutputBasename)

        // whatever happens, we need a transcoder output FIFO (even if there's a match error, we carry
        // on with the unmodified URI), so we can create that upfront
        processManager.createTranscoderFifo(transcoderOutputBasename)

        def ffmpegPath = normalizePath(configuration.getFfmpegPath())
        def mencoderPath = normalizePath(configuration.getMencoderPath())
        def mencoderMtPath = normalizePath(configuration.getMencoderMTPath())
        def mplayerPath = normalizePath(configuration.getMplayerPath())
        def response = plugin.match('pmsencoder', oldURI, dlna, media, params)
        def mimeType = response['mimeType']

        if (mimeType != null) {
            logger.debug('thread id: ' + threadId)
            logger.info("setting custom mime-type: ${mimeType}")
            threadLocal.set(mimeType)
        } else {
            threadLocal.remove() // remove it to prevent memory leaks
        }

        def newURI = Util.quoteURI(response['uri'])

        if (response.hook) {
            processManager.attachAndStartCommand(response.hook.toList(newURI))
        }

        // FIXME: groovy++ type inference fail: the subscript and/or concatenation operations
        // on downloaderCmd and transcoderCmd are causing groovy++ to define them as
        // Collection<String> rather than List<String>
        List<String> downloaderCmd, transcoderCmd

        // Groovy's "documentation" doesn't make it clear whether local variables are null-initialized
        // http://stackoverflow.com/questions/4025222
        def commandProcess = null

        if (response.downloader) {
            downloaderCmd = response.downloader.toList(newURI, downloaderOutputPath)
            transcoderCmd = response.transcoder.toList(isWindows ? '-' : downloaderOutputPath, transcoderOutputPath)

            if (isWindows) {
                transcoderCmd = [ 'win32/mcfifo', downloaderOutputPath ] + transcoderCmd
                processManager.attachAndStartCommand(transcoderCmd, true)
                commandProcess = processManager.attachCommand(downloaderCmd)
            } else {
                processManager.createDownloaderFifo(downloaderOutputBasename)
                processManager.attachAndStartCommand(downloaderCmd)
                commandProcess = processManager.attachCommand(transcoderCmd, true)
            }
        } else {
            transcoderCmd = response.transcoder.toList(newURI, transcoderOutputPath)
            commandProcess = processManager.attachCommand(transcoderCmd, true)
        }

        processManager.startCommandProcess(commandProcess)
    }
}
