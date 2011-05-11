@Typed
package com.chocolatey.pmsencoder

import static groovy.io.FileType.FILES

import net.pms.PMS

enum Stage { BEGIN, INIT, SCRIPT, CHECK, END }

class PMSConf { // no need to extend HashMap<...>: we only need the subscript - i.e. getAt() - syntax
    public String getAt(String key) {
        return PMS.getConfiguration().getCustomProperty(key?.toString())
    }
}

// XXX note: only public methods can be delegated to
class Matcher implements LoggerMixin {
    private Map<String, Profile> profiles = new LinkedHashMap<String, Profile>()
    private GroovyShell groovy
    PMS pms
    List<Integer> youtubeAccept = []
    PMSConf pmsConf = new PMSConf()
    // @Lazy HTTPClient http = new HTTPClient()
    HTTPClient http = new HTTPClient()
    Stash stash = new Stash()
    static Class<? extends Transcoder> defaultTranscoderClass = Ffmpeg.class

    Matcher(PMS pms) {
        this.pms = pms

        def binding = new Binding(
            begin:  this.&begin,
            init:   this.&init,
            script: this.&script,
            check:  this.&check,
            end:    this.&end
        )

        groovy = new GroovyShell(binding)
    }

    static Transcoder createDefaultTranscoder() {
        return defaultTranscoderClass.newInstance()
    }

    private URL getResource(String name) {
        return this.getClass().getResource("/${name}");
    }

    // production interface (via PMSEncoder -> Plugin)
    Response match(Request request) {
        def response = new Response(request)
        match(response, true)
    }

    // test interface (chiefly via PMSEncoderTestCase)
    // write this out explicitly to avoid issues with default args and delegation
    Response match(Response response) {
        match(response, true)
    }

    Response match(Response response, boolean useDefault) {
        try {
            if (useDefault) {
                response.transcoder = createDefaultTranscoder()
            }

            assert response.transcoder != null

            def uri = response['uri']
            logger.debug("matching URI: ${uri}")

            // XXX this is horribly inefficient, but it's a) trivial to implement and b) has the right semantics.
            // the small number of scripts make this a non-issue for now
            Stage.each { stage ->
                profiles.values().each { profile ->
                    if (profile.stage == stage && profile.match(response)) {
                        // XXX make sure we take the name from the profile itself
                        // rather than the Map key - the latter may have been usurped
                        // by a profile with a different name
                        response.matches << profile.name
                    }
                }
            }

            def matched = response.matches.size() > 0

            if (matched) {
                logger.trace("response: ${response}")
            }
        } catch (Throwable e) {
            logger.error('match error: ' + e)
            PMS.error('PMSEncoder: match error', e)
        }

        return response
    }

    // a Profile consists of a name, a pattern block and an action block - all
    // determined when the script is loaded/compiled
    public void registerProfile(String name, Stage stage, Map<String, String> options, Closure closure) {
        def extendz = options['extends']
        def replaces = options['replaces']
        def target

        if (replaces != null) {
            target = replaces
            logger.info("replacing profile ${replaces} with: ${name}")
        } else {
            target = name
            if (profiles[name] != null) {
                logger.info("replacing profile: ${name}")
            } else {
                logger.info("registering ${stage.toString().toLowerCase()} profile: ${name}")
            }
        }

        def profile = new Profile(this, name, stage)

        try {
            // run the profile block at compile-time to extract its (optional) pattern and action blocks,
            // but invoke them at runtime
            profile.extractBlocks(closure)

            if (extendz != null) {
                if (profiles[extendz] == null) {
                    logger.error("attempt to extend a nonexistent profile: ${extendz}")
                } else {
                    def base = profiles[extendz]
                    profile.assignPatternBlockIfNull(base)
                    profile.assignActionBlockIfNull(base)
                }
            }

            // this is why name is defined both as the key of the map and in the profile
            // itself. the key allows replacement
            profiles[target] = profile
        } catch (Throwable e) {
            logger.error("invalid profile (${name}): " + e.getMessage())
        }
    }

    void load(String path, String filename = path) {
        load(new File(path), filename)
    }

    void load(URL url, String filename = url.toString()) {
        load(url.openStream(), filename)
    }

    void load(File file, String filename = file.getPath()) {
        load(new FileInputStream(file), filename)
    }

    void load(InputStream stream, String filename) {
        load(new InputStreamReader(stream), filename)
    }

    // we could impose a constraint here that a script (file) must
    // contain exactly one stage block, but why bother?
    void load(Reader reader, String filename) {
        groovy.evaluate(reader, filename)
    }

    void loadUserScripts(File scriptDirectory) {
        if (!scriptDirectory.isDirectory()) {
            logger.error("invalid user script directory (${scriptDirectory}): not a directory")
        } else if (!scriptDirectory.exists()) {
            logger.error("invalid user script directory (${scriptDirectory}): directory doesn't exist")
        } else {
            logger.info("loading user scripts from: ${scriptDirectory}")
            scriptDirectory.eachFileRecurse(FILES) { File file ->
                def filename = file.getName()
                if (filename.endsWith('.groovy')) {
                    logger.info("loading user script: ${filename}")
                    try {
                        load(file)
                    } catch (Exception e) {
                        def path = file.getAbsolutePath()
                        logger.error("can't load user script: ${path}", e)
                    }
                }
            }
        }
    }

    void loadDefaultScripts() {
        logger.info('loading built-in scripts')

        getResource('lib.txt').eachLine() { String scriptName ->
            logger.info("loading built-in script: ${scriptName}")
            def scriptURL = getResource(scriptName)
            if (scriptURL == null) {
                logger.error("can't load ${scriptURL}")
            } else {
                load(scriptURL)
            }
        }
    }

    String getAt(String name) {
        return stash[name]
    }

    String putAt(String name, Object value) {
        return stash[name] = value?.toString()
    }

    // DSL method
    String propertyMissing(String name) {
        logger.trace("retrieving global variable: ${name}")
        return stash[name]
    }

    // DSL method
    String propertyMissing(String name, Object value) {
        logger.info("setting global variable: ${name} = ${value}")
        return stash[name] = value
    }

    // DSL method
    protected void begin(Closure closure) {
        closure.delegate = new Script(this, Stage.BEGIN)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

    // DSL method
    protected void init(Closure closure) {
        closure.delegate = new Script(this, Stage.INIT)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

    // DSL method
    protected void script(Closure closure) {
        closure.delegate = new Script(this, Stage.SCRIPT)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

    // DSL method
    protected void check(Closure closure) {
        closure.delegate = new Script(this, Stage.CHECK)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }

    // DSL method
    protected void end(Closure closure) {
        closure.delegate = new Script(this, Stage.END)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()
    }
}
