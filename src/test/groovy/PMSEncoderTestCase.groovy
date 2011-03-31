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
        }

        new MockUp<PMS>() {
            static final PmsConfiguration pmsConfig = new PmsConfiguration()

            @Mock
            public boolean init () { true }

            @Mock
            public static void minimal(String msg) {
                println msg
            }

            @Mock
            public static PmsConfiguration getConfiguration() { pmsConfig }
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
            stash = new Stash([ $URI: uri ])
        }

        List<String> wantMatches = getValue(spec, 'wantMatches')
        List<String> hook = getValue(spec, 'hook')
        List<String> downloader = getValue(spec, 'downloader')
        List<String> transcoder = getValue(spec, 'transcoder')
        List<String> output = getValue(spec, 'output')

        def wantStash = getValue(spec, 'wantStash')
        def wantHook = getValue(spec, 'wantHook')
        def wantDownloader = getValue(spec, 'wantDownloader')
        def wantTranscoder = getValue(spec, 'wantTranscoder')
        def wantOutput = getValue(spec, 'wantOutput')

        boolean useDefaultTranscoder = getValue(spec, 'useDefaultTranscoder', false)

        if (scripts != null) {
            scripts.each {
                assert it != null
                matcher.load(it)
            }
        }

        def response

        if (transcoder != null) {
            if (stash != null) {
                response = new Response(stash, transcoder)
            } else {
                response = new Response(transcoder)
            }
        } else if (stash != null) {
            response = new Response(stash)
        } else {
            response = new Response()
        }

        if (hook != null) {
            response.hook = hook
        }

        if (downloader != null) {
            response.downloader = downloader
        }

        if (transcoder != null) {
            response.transcoder = transcoder
        }

        if (output != null) {
            response.output = output
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

        if (wantHook != null) {
            if (wantHook instanceof Closure) {
                assert (wantHook as Closure).call(response.hook)
            } else {
                assert response.hook == wantHook
            }
        }

        if (wantDownloader != null) {
            if (wantDownloader instanceof Closure) {
                assert (wantDownloader as Closure).call(response.downloader)
            } else {
                assert response.downloader == wantDownloader
            }
        }

        if (wantTranscoder != null) {
            if (wantTranscoder instanceof Closure) {
                assert (wantTranscoder as Closure).call(response.transcoder)
            } else {
                assert response.transcoder == wantTranscoder
            }
        }

        if (wantOutput != null) {
            if (wantOutput instanceof Closure) {
                assert (wantOutput as Closure).call(response.output)
            } else {
                assert response.output == wantOutput
            }
        }
    }
}
