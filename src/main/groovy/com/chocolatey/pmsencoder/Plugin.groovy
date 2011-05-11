@Typed
package com.chocolatey.pmsencoder

import static com.chocolatey.pmsencoder.Util.guard
import static com.chocolatey.pmsencoder.Util.fileExists
import static com.chocolatey.pmsencoder.Util.directoryExists

import static groovy.io.FileType.FILES
import groovy.swing.SwingBuilder // TODO

import javax.swing.JComponent
import javax.swing.JFrame

import net.pms.configuration.PmsConfiguration
import net.pms.dlna.DLNAMediaInfo
import net.pms.dlna.DLNAResource
import net.pms.encoders.Player
import net.pms.external.ExternalListener
import net.pms.formats.Format
import net.pms.PMS

import no.geosoft.cc.io.FileListener
import no.geosoft.cc.io.FileMonitor

import org.apache.log4j.xml.DOMConfigurator

class Plugin implements ExternalListener, FileListener {
    private static final String VERSION = '1.6.0'
    private static final String DEFAULT_SCRIPT_DIRECTORY = 'pmsencoder'
    private static final String LOG_CONFIG = 'pmsencoder.logger.config'
    private static final String SCRIPT_DIRECTORY = 'pmsencoder.script.directory'
    private static final String SCRIPT_POLL = 'pmsencoder.script.poll'
    // 1 second is flaky - it results in overlapping file change events
    private static final int MIN_SCRIPT_POLL_INTERVAL = 2

    private PMSEncoder pmsencoder
    private FileMonitor fileMonitor
    private File scriptDirectory
    private long scriptPollInterval
    private Matcher matcher
    private PmsConfiguration configuration
    private PMS pms
    private Object lock = new Object()

    public Plugin() {
        info('initializing PMSEncoder ' + VERSION)
        pms = PMS.get()
        configuration = PMS.getConfiguration()

        // get optional overrides from PMS.conf
        String customLogConfigPath = configuration.getCustomProperty(LOG_CONFIG)
        String candidateScriptDirectory = configuration.getCustomProperty(SCRIPT_DIRECTORY)

        /*
           XXX: When Groovy breaks down...

           long-windedness is required here to ensure that a string is correctly converted to
           an Integer. in a previous incarnation:

                pmsencoder.script.poll = 2

            resulted in a poll interval of 50 (i.e. the ASCII value of the character "2")
        */

        // cast the expression to the type of the default value (int) and return the default value
        // (0) if an exception (in this case a java.lang.NumberFormatException) is thrown
        String scriptPollString = configuration.getCustomProperty(SCRIPT_POLL)
        // changing this "int" to "def" used to produce a Verify error (see TODO.groovy passim),
        // but as of Groovy++ 0.4.180 (or thereabouts) gives a more helpful error
        int candidateScriptPollInterval = guard (0) { scriptPollString.toInteger() }

        // handle scripts
        if (candidateScriptDirectory) {
            def candidateScriptDirectoryFile = new File(candidateScriptDirectory)

            if (directoryExists(candidateScriptDirectoryFile)) {
                scriptDirectory = candidateScriptDirectoryFile.getAbsoluteFile()
            } else {
                def absPath = candidateScriptDirectoryFile.getAbsolutePath()
                error("invalid path for script directory ($absPath): no such directory", null)
            }
        } else {
            def candidateScriptDirectoryFile = new File(DEFAULT_SCRIPT_DIRECTORY)

            if (directoryExists(candidateScriptDirectoryFile)) {
                scriptDirectory = candidateScriptDirectoryFile.getAbsoluteFile()
            }
        }

        if (scriptDirectory) {
            info("script directory: $scriptDirectory")

            if (candidateScriptPollInterval > 0) {
                if (candidateScriptPollInterval < MIN_SCRIPT_POLL_INTERVAL) {
                    candidateScriptPollInterval = MIN_SCRIPT_POLL_INTERVAL
                }
                info("setting polling interval to $candidateScriptPollInterval seconds")
                scriptPollInterval = candidateScriptPollInterval * 1000
                monitorScriptDirectory()
            }
        }

        // set up log4j
        def customLogConfig

        if (customLogConfigPath) {
            def customLogConfigFile = new File(customLogConfigPath)

            if (fileExists(customLogConfigFile)) {
                customLogConfig = customLogConfigFile.getAbsolutePath()
            } else {
                def absPath = customLogConfigFile.getAbsolutePath()
                error("invalid path for log4j config file ($absPath): no such file", null)
            }
        }

        // load log4j config file
        if (customLogConfig) {
            info("loading custom log4j config file: $customLogConfig")

            try {
                DOMConfigurator.configure(customLogConfig)
            } catch (Exception e) {
                error("error loading log4j config file ($customLogConfig)", e)
                loadDefaultLogConfig()
            }
        } else {
            loadDefaultLogConfig()
        }

        // make sure we have a matcher before we create the transcoding engine
        createMatcher()

        // initialize the transcoding engine
        pmsencoder = new PMSEncoder(configuration, this)

        /*
         * FIXME: don't assume the position is fixed
         * short term: find and replace *if it exists*
         * long term: patch PMS to allow plugins to register engines a) separately and b) cleanly
         * */
        def extensions = pms.getExtensions()
        extensions.set(0, new WEB())
        registerPlayer(pmsencoder)
    }

    private void loadDefaultLogConfig() {
        // XXX squashed bug - don't call this log4j.xml, as, by default,
        // log4j attempts to load log4j.properties and log4j.xml automatically
        def defaultLogConfig = this.getClass().getResource('/log4j_default.xml')
        info("loading built-in log4j config file: $defaultLogConfig")

        try {
            DOMConfigurator.configure(defaultLogConfig)
        } catch (Exception e) {
            error("error loading built-in log4j config file ($defaultLogConfig)", e)
        }
    }

    private void info(String message) {
        PMS.minimal("PMSEncoder: $message")
    }

    private void error(String message, Exception e) {
        PMS.error("PMSEncoder: $message", e)
    }

    private void monitorScriptDirectory() {
        fileMonitor = new FileMonitor(scriptPollInterval)
        fileMonitor.addFile(scriptDirectory)
        fileMonitor.addListener(this)
    }

    public void fileChanged(File file) {
        info("$file has changed; reloading scripts")
        createMatcher()
    }

    private void createMatcher() {
        synchronized (lock) {
            matcher = new Matcher(pms)

            try {
                matcher.loadDefaultScripts()

                if (directoryExists(scriptDirectory)) {
                    matcher.loadUserScripts(scriptDirectory)
                }
            } catch (Exception e) {
                error('error loading scripts', e)
            }
        }
    }

    private void registerPlayer(PMSEncoder pmsencoder) {
        try {
            def pmsRegisterPlayer = pms.getClass().getDeclaredMethod('registerPlayer', Player.class)
            pmsRegisterPlayer.setAccessible(true)
            pmsRegisterPlayer.invoke(pms, pmsencoder)
        } catch (Exception e) {
            error('error calling PMS.registerPlayer', e)
        }
    }

    public Response match(Request request) {
        matcher.match(request)
    }

    @Override
    public JComponent config() {
        return null
    }

    @Override
    public String name() {
        return 'PMSEncoder plugin for PS3 Media Server'
    }

    @Override
    public void shutdown () {
        if (fileMonitor != null) {
            fileMonitor.stop()
        }
    }
}
