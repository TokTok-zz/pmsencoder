@Typed
package com.chocolatey.pmsencoder

import groovy.util.GroovyTestCase
import mockit.*
import net.pms.configuration.PmsConfiguration
import net.pms.PMS
import org.apache.log4j.xml.DOMConfigurator

// there's no point trying to optimize this while we're still using JUnit:
// http://tinyurl.com/6k6z6dj
abstract class PMSEncoderTestCase extends GroovyTestCase {
    protected Matcher matcher
    private PMS pms
    private URL defaultScript

    void setUp() {
        def log4jConfig = this.getClass().getResource('/log4j_test.xml')
        DOMConfigurator.configure(log4jConfig)

        defaultScript = this.getClass().getResource('/DEFAULT.groovy')

        new MockUp<PmsConfiguration>() {
            @Mock
            public int getNumberOfCpuCores() { 3 }

            @Mock
            public Object getCustomProperty(String key) {
                return key == 'rtmpdump.path' ? '/usr/bin/rtmpdump' : null
            }

            @Mock
            public String getFfmpegPath() { 'ffmpeg' }

            @Mock
            public String getMencoderPath() { 'mencoder' }

            @Mock
            public String getMencoderMTPath() { 'mencoder_mt' }

            @Mock
            public String getMplayerPath() { 'mplayer' }

            @Mock
            public String getVlcPath() { 'vlc' }
        }

        new MockUp<PMS>() {
            static final PmsConfiguration pmsConfig = new PmsConfiguration()

            @Mock
            public boolean init () { true }

            @Mock
            public static PmsConfiguration getConfiguration() { pmsConfig }

            @Mock
            public static void minimal(String msg) {
                println "PMS info: " + msg
            }

            @Mock
            public static void error(String msg, Throwable t) {
                print "PMS error: $msg"
                t.printStackTrace()
            }
        }

        pms = PMS.get()
        matcher = new Matcher(pms)
    }

    private Object getValue(Map<String, Object> map, String key, Object defaultValue = null) {
        if (map.containsKey(key)) {
            return map[key]
        } else {
            return defaultValue
        }
    }

    // allow ProfileDelegate methods to be tested without having to do so indirectly through scripts
    public ProfileDelegate getProfileDelegate() {
        return new ProfileDelegate(matcher, new Response(new TestTranscoder()))
    }

    protected void assertMatch(Map<String, Object> spec) {
        if (spec['loadDefaultScripts']) {
            matcher.loadDefaultScripts()
        }

        List<URL> scripts

        if (spec['script'] != null) {
            if (!(spec['script'] instanceof List)) {
                spec['script'] = [ spec['script'] ]
            }

            scripts = spec['script'].collect {
                def url

                if (it instanceof URL) {
                    url = it
                } else {
                    url = this.getClass().getResource(it as String)
                }

                assert url != null
                return url
            }
        }

        Stash stash

        if (spec.containsKey('stash')) {
            Map<String, String> map = spec['stash']
            stash = new Stash(map)
        } else { // uri can be null (not all tests need it)
            String uri = spec['uri']
            stash = new Stash([ uri: uri ])
        }

        List<String> wantMatches = getValue(spec, 'wantMatches')
        List<String> hookList = getValue(spec, 'hook')
        List<String> downloaderList = getValue(spec, 'downloader')
        List<String> transcoderList = getValue(spec, 'transcoder')
        List<String> outputList = getValue(spec, 'output')

        def wantStash = getValue(spec, 'wantStash')
        def wantHook = getValue(spec, 'wantHook')
        def wantDownloader = getValue(spec, 'wantDownloader')
        def wantTranscoder = getValue(spec, 'wantTranscoder')

        boolean useDefaultTranscoder = getValue(spec, 'useDefaultTranscoder', false)

        if (scripts != null) {
            scripts.each {
                assert it != null
                matcher.load(it)
            }
        }

        // populate the response
        def response, hook, downloader
        def transcoder = transcoderList ? Util.toCommand(TestTranscoder.class, transcoderList) : new TestTranscoder()

        if (stash != null) {
            response = new Response(stash, transcoder)
        } else {
            response = new Response(transcoder)
        }

        if (hookList != null) {
            // FIXME: why doesn't assignment via response.hook = ... work?
            response.setHook(hookList)
        }

        if (downloaderList != null) {
            // FIXME: cryptic bytecode error when assigning via downloader = ...
            response.setDownloader(downloaderList)
        }

        if (outputList != null) {
            response.transcoder.setOutput(outputList)
        }

        matcher.match(response, useDefaultTranscoder)

        if (wantMatches != null) {
            assert response.matches == wantMatches
        }

        /*
           XXX

            Groovy(++) bug: strongly-typing the wantStash and wantTranscoder closures
            results in an exception when the closure contains a String =~ String expression
            (i.e. returns a Matcher):

                java.lang.ClassCastException: java.util.regex.Matcher cannot be cast to java.lang.Boolean

            This contradicts TFM.

            Loosely-typing them as mere Closures works around this.
        */

        if (wantStash != null) {
            if (wantStash instanceof Closure) {
                assert (wantStash as Closure).call(response.stash)
            } else {
                assert response.stash == wantStash
            }
        }

        String uri = stash['uri'] // possibly null

        if (wantHook != null) {
            assert response.hook != null
            def gotHook = response.hook.toList(uri)
            if (wantHook instanceof Closure) {
                assert (wantHook as Closure).call(gotHook)
            } else {
                assert gotHook == wantHook
            }
        }

        if (wantDownloader != null) {
            assert response.downloader != null
            def gotDownloader = response.downloader.toList(uri, 'DOWNLOADER_OUT') // preserve the default
            if (wantDownloader instanceof Closure) {
                assert (wantDownloader as Closure).call(gotDownloader)
            } else {
                assert gotDownloader == wantDownloader
            }
        }

        if (wantTranscoder != null) {
            assert response.transcoder != null
            def gotTranscoder = response.transcoder.toList(uri, 'TRANSCODER_OUT') // preserve the default
            if (wantTranscoder instanceof Closure) {
                assert (wantTranscoder as Closure).call(gotTranscoder)
            } else {
                assert gotTranscoder == wantTranscoder
            }
        }
    }
}
