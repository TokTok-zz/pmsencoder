@Typed
package com.chocolatey.pmsencoder

import net.pms.io.OutputParams

/*
 * this object encapsulates the per-request state passed from the plugin to the transcode launcher (PMSEncoder.java).
 */
class Response implements LoggerMixin {
    Stash stash
    List<String> matches = []
    Request request
    // try to work around Groovy's labyrinthine fail: setters and getters effectively don't work unless
    // written by hand
    // http://groovy.329449.n5.nabble.com/When-setters-setProperty-do-or-don-t-get-called-tp510182p510182.html
    private Command hook
    private Downloader downloader
    private Transcoder transcoder

    public Response() {
        this(new Stash(), Matcher.createDefaultTranscoder())
    }

    public Response(Stash stash) {
        this(stash, Matcher.createDefaultTranscoder())
    }

    public Response(Map map) {
        this(new Stash(map), Matcher.createDefaultTranscoder())
    }

    public Response(Transcoder transcoder) {
        this(new Stash(), transcoder)
    }

    public Response(Stash stash, Transcoder transcoder) {
        this.stash = stash
        this.transcoder = transcoder
    }

    // convenience constructor: allow the stash to be supplied as a Map<String, Object>
    // e.g. new Response([ uri: uri ])
    // FIXME: do we need this?
    /*
    public Response(Map<String, String> map) {
        this(new Stash(map), Matcher.createDefaultTranscoder())
    }
    */

    public Response(Request request) {
        this([ uri: request.uri ])
        this.request = request
    }

    public Response(Response other) {
        def copy = new Response(new Stash(other.stash), new Transcoder(other.transcoder))
        copy.downloader = new Downloader(other.downloader)
        copy.hook = new Command(other.hook)
        copy.matches = new ArrayList<String>(other.matches)
        copy.request = new Request(other.request)
        copy
    }

    public boolean equals(Response other) {
        this.downloader == other.downloader &&
        this.hook == other.hook &&
        this.matches == other.matches &&
        this.request == other.request &&
        this.stash == other.stash &&
        this.transcoder == other.transcoder
    }

    public java.lang.String toString() {
        def repr = """
        {
            downloader: $downloader
            hook:       $hook
            matches:    $matches
            request:    $request
            stash:      $stash
            transcoder: $transcoder
        }""".substring(1).stripIndent(8)

    }

    protected String getAt(String name) {
        stash[name]
    }

    protected String putAt(String name, Object value) {
        stash[name] = value
    }

    // setter implementation with logged stash assignments
    public String let(Object name, Object value) {
        def strValue = value?.toString()
        if (!stash.containsKey(name) || (stash[name] != strValue)) {
            logger.debug("setting $name to $strValue")
        }
        stash[name] = strValue
        return strValue // for chaining: foo = bar = baz i.e. foo = (bar = baz)
    }

    /*
        XXX Groovy fail: http://jira.codehaus.org/browse/GROOVY-2500

        if two or more setters for a property (e.g. downloader) are defined (e.g. one for String and another
        for List<String>) Groovy/Groovy++ only uses one of them, complaining at runtime that
        it can't cast e.g. a String into a List:

            Cannot cast object '/usr/bin/downloader string http://downloader.string'
            with class 'java.lang.String' to class 'java.util.List'

        workaround: define just one setter and determine the type with instanceof (via toCommand)
    */

    // DSL accessor (downloader): getter
    public Downloader getDownloader() {
        downloader
    }

    // DSL accessor (downloader): setter
    public Downloader setDownloader(Object downloader) {
        this.downloader = Util.toCommand(Downloader.class, downloader)
    }

    // DSL accessor (transcoder): getter
    public Transcoder getTranscoder() {
        transcoder
    }

    // DSL accessor (transcoder): setter
    public Transcoder setTranscoder(Object transcoder) {
        this.transcoder = Util.toCommand(Transcoder.class, transcoder)
    }

    // DSL accessor (hook): getter
    public Command getHook() {
        hook
    }

    // DSL accessor (hook): setter
    public Command setHook(Object hook) {
        this.hook = Util.toCommand(Command.class, hook)
    }

    // FIXME: use the URI class
    private String getProtocol(String uri) {
        if (uri != null) {
            return RegexHelper.match(uri, '^(\\w+)://')[1]
        } else {
            return null
        }
    }

    // protocol: getter
    public String getProtocol() {
        return getProtocol(this['uri'])
    }
}
