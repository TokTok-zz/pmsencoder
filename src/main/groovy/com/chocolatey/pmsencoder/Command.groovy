@Typed

package com.chocolatey.pmsencoder

import groovy.transform.*

class Command {
    private static String defaultExecutable = 'command'
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    // XXX try to work around Groovy's setter/getter bypassing fail by writing getters/setters by hand:
    // http://groovy.329449.n5.nabble.com/When-setters-setProperty-do-or-don-t-get-called-tp510182p510182.html
    private String executable
    private List<String> args
    private List<String> output

    Command() {
        this(getDefaultExecutable(), getDefaultArgs(), getDefaultOutputArgs())
    }

    Command(String executable) {
        this(executable, getDefaultArgs(), getDefaultOutputArgs())
    }

    Command(List args) {
        this(getDefaultExecutable(), args, getDefaultOutputArgs())
    }

    Command(String executable, List args) {
        this(executable, args, getDefaultOutputArgs())
    }

    Command(String executable, List args, List output) {
        def className = this.class.name
        assert executable != null : "executable not defined for ${className}"
        this.executable = executable
        assert args != null : "args not defined for ${className}"
        this.args = args*.toString()
        assert output != null : "output not defined for ${className}"
        this.output = output*.toString()
    }

    Command(Command other) {
        this(other.executable, cloneArgs(other.args), cloneArgs(other.output))
    }

    String toString() {
        "{ executable: $executable, args: $args, output: $output }"
    }

    // XXX: squashed bug: static: needs to be available in a constructor e.g. before instance is available
    static protected List<String> cloneArgs(List<String> args) {
        new ArrayList<String>(args)
    }

    public String getExecutable() {
        this.executable
    }

    public String setExecutable(Object executable) {
        this.executable = executable?.toString()
    }

    public List<String> getArgs() {
        this.args
    }

    public List<String> setArgs(Object stringOrList) {
        this.args = Util.toStringList(stringOrList)
    }

    public List<String> getOutput() {
        this.output
    }

    public List<String> setOutput(Object stringOrList) {
        this.output = Util.toStringList(stringOrList)
    }

    public static String getDefaultExecutable() {
        this.defaultExecutable
    }

    public static setDefaultExecutable(String executable) {
        this.defaultExecutable = executable
    }

    public static List<String> getDefaultArgs() {
        cloneArgs(this.defaultArgs)
    }

    public static List<String> setDefaultArgs(Object stringOrList) {
        this.defaultArgs = Util.toStringList(stringOrList)
    }

    public static List<String> getDefaultOutputArgs() {
        cloneArgs(this.defaultOutputArgs)
    }

    public static List<String> setDefaultOutputArgs(Object stringOrList) {
        this.defaultOutputArgs = Util.toStringList(stringOrList)
    }

    private List<String> expandMacros(List<String> list, String uri) {
        list.collect { it.replaceAll('\\bURI\\b', uri) }
    }

    protected List<String> toList(String uri) {
        [ executable ] + expandMacros(args, uri) + expandMacros(output, uri)
    }
}

@InheritConstructors
class Downloader extends Command {
    private List<String> expandMacros(List<String> list, String uri, String downloaderOut) {
        list.collect { it.replaceAll('\\bURI\\b', uri).replaceAll('\\bDOWNLOADER_OUT\\b', downloaderOut) }
    }

    protected List<String> toList(String uri, String downloaderOut) {
        [ executable ] + expandMacros(args, uri, downloaderOut) + expandMacros(output, uri, downloaderOut)
    }
}

@InheritConstructors
class Transcoder extends Command {
    private List<String> expandMacros(List<String> list, String uri, String transcoderOut) {
        list.collect { it.replaceAll('\\bURI\\b', uri).replaceAll('\\bTRANSCODER_OUT\\b', transcoderOut) }
    }

    protected List<String> toList(String uri, String transcoderOut) {
        [ executable ] + expandMacros(args, uri, transcoderOut) + expandMacros(output, uri, transcoderOut)
    }
}

@InheritConstructors
class MPlayer extends Downloader {
    private static String defaultExecutable = Platform.MPLAYER_PATH
}

@InheritConstructors
class MEncoder extends Command {
    private static String defaultExecutable = Platform.MENCODER_PATH
}

@InheritConstructors
class Ffmpeg extends Command {
    private static String defaultExecutable = Platform.FFMPEG_PATH
}
