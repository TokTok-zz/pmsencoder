@Typed
package com.chocolatey.pmsencoder

import net.pms.io.OutputParams
import net.pms.io.PipeProcess
import net.pms.io.ProcessWrapper
import net.pms.io.ProcessWrapperImpl
import net.pms.PMS

private class ProcessManager implements LoggerMixin {
    static final long LAUNCH_TRANSCODE_SLEEP = 200
    static final long MKFIFO_SLEEP = 200
    List<ProcessWrapper> attachedProcesses
    OutputParams outputParams
    private PMSEncoder pmsencoder

    ProcessManager(PMSEncoder pmsencoder, OutputParams params) {
        this.pmsencoder = pmsencoder
        this.outputParams = params
        attachedProcesses = new ArrayList<ProcessWrapper>()
        // modify the output params object *before* the match so it can optionally be customized
        outputParams.minBufferSize = params.minFileSize
        outputParams.secondread_minsize = 100000
        outputParams.log = true // for documentation only as it's done automatically for pipe-writing processes
    }

    private String[] listToArray(List<String> list) {
        def array = new String[ list.size() ]
        list.toArray(array)
        return array
    }

    private void sleepFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds)
        } catch (InterruptedException e) {
            PMS.error('PMSEncoder: thread interrupted', e)
        }
    }

    private PipeProcess mkfifo(PipeProcess pipe) {
        def process = pipe.getPipeProcess()
        attachedProcesses << process
        process.runInNewThread()
        sleepFor(MKFIFO_SLEEP)
        pipe.deleteLater()
        return pipe
    }

    public String getFifoPath(String basename) {
        try {
            return pmsencoder.isWindows ?
                '\\\\.\\pipe\\' + basename :
                (new File(PMS.getConfiguration().getTempFolder(), basename)).getCanonicalPath()
        } catch (IOException e) {
            PMS.error('PMSEncoder: Pipe may not be in temporary directory', e)
            return basename
        }
    }

    public void createTranscoderFifo(String transcoderOutputBasename) {
        def transcoderOutputPipe = mkfifo(new PipeProcess(transcoderOutputBasename))

        // this file is the one that PMS reads the transcoded video from via params.input_pipes[0],
        // so we can assign that upfront as well
        outputParams.input_pipes[0] = transcoderOutputPipe
    }

    public void createDownloaderFifo(String downloaderOutputBasename) {
        mkfifo(new PipeProcess(downloaderOutputBasename))
    }

    public void attachAndStartCommand(List<String> cmdArgs, useOutputParams = false) {
        def cmdArray = listToArray(cmdArgs)
        def params

        if (useOutputParams) {
            params = outputParams
        } else {
            // PMS doesn't require input from this process - so use new OutputParams
            params = new OutputParams(pmsencoder.getConfiguration())
            params.log = true
        }

        def process = new ProcessWrapperImpl(cmdArray, params)

        attachedProcesses << process
        logger.info('starting command: ' + Arrays.toString(cmdArray))
        process.runInNewThread()
    }

    public ProcessWrapperImpl attachCommand(List<String> cmdArgs, boolean useOutputParams = false) {
        def cmdArray = listToArray(cmdArgs)
        def params

        if (useOutputParams) {
            params = outputParams
        } else {
            // PMS doesn't require input from this process - so use new OutputParams
            params = new OutputParams(pmsencoder.getConfiguration())
            params.log = true
        }

        def process = new ProcessWrapperImpl(cmdArray, outputParams) // may modify cmdArray[0]
        attachedProcesses.each { process.attachProcess(it) }
        logger.info('starting command: ' + Arrays.toString(cmdArray))
        return process
    }

    public ProcessWrapper startCommandProcess(ProcessWrapperImpl process) {
        sleepFor(3000)
        process.runInNewThread()
        sleepFor(LAUNCH_TRANSCODE_SLEEP)
        return process
    }
}
