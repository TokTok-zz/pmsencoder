@Typed
package com.chocolatey.pmsencoder

import net.pms.io.OutputParams

import org.apache.log4j.Level

/*
 * this object encapsulates the per-request state passed from the plugin to the transcode launcher (PMSEncoder.java).
 */
class Response implements LoggerMixin, Cloneable {
    Stash stash
    OutputParams params
    Level stashAssignmentLogLevel = Level.DEBUG
    List<String> matches = []
    List<String> hook = []
    List<String> downloader = []
    List<String> transcoder = []
    List<String> output = []

    private Response(Stash stash, List<String> transcoder, List<String> matches) {
        this.stash = stash
        this.transcoder = transcoder
        this.matches = matches
    }

    public Response() {
        this(new Stash(), [], [])
    }

    public Response(Stash stash) {
        this(stash, [])
    }

    public Response(List<String> transcoder) {
        this(new Stash(), transcoder)
    }

    public Response(Stash stash, List<String> transcoder) {
        this(stash, transcoder, [])
    }

    // convenience constructor: allow the stash to be supplied as a Map<String, String>
    // e.g. new Response([ uri: uri ])
    public Response(Map<String, String> map) {
        this(new Stash(map), [], [])
    }

    public Response(Request request) {
        this([ uri: request.uri ])
        this.params = request.params
    }

    public Response(Response other) {
        this(new Stash(other.stash), new ArrayList<String>(other.transcoder), new ArrayList<String>(other.matches))
    }

    public Response clone() {
        return new Response(this)
    }

    public void setParams(OutputParams params) {
        this.params = params
    }

    public boolean equals(Response other) {
        this.matches == other.matches &&
        this.hook == other.hook &&
        this.downloader == other.downloader &&
        this.transcoder == other.transcoder &&
        this.output == other.output &&
        this.stash == other.stash &&
        this.params == other.params
    }

    public java.lang.String toString() {
        // can't stringify params until this patch has been applied:
        // https://code.google.com/p/ps3mediaserver/issues/detail?id=863
        def repr = """
        {
            matches:    $matches
            hook:       $hook
            downloader: $downloader
            transcoder: $transcoder
            output:     $output
            params:     $params
            stash:      $stash
        }""".substring(1).stripIndent(8)

    }

    protected boolean hasVar(Object name) {
        stash.containsKey(name)
    }

    protected String getVar(Object name) {
        stash.get(name)
    }

    protected String setVar(Object name, Object value) {
        let(name, value)
    }

    // setter implementation with logged stash assignments
    public String let(Object name, Object value) {
        if ((stash.get(name) == null) || (stash.get(name) != value.toString())) {
            if (stashAssignmentLogLevel != null) {
                log.log(stashAssignmentLogLevel, "setting $name to $value")
            }
            stash.put(name, value)
        }

        return value // for chaining: foo = bar = baz i.e. foo = (bar = baz)
    }
}
