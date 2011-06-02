@Typed

package com.chocolatey.pmsencoder

import groovy.transform.*

@AutoClone
class Command {
    // XXX try to work around Groovy's setter/getter bypassing fail by writing getters/setters by hand:
    // http://groovy.329449.n5.nabble.com/When-setters-setProperty-do-or-don-t-get-called-tp510182p510182.html
    private String executable
    private List<String> args
    private List<String> output

    // XXX: currently needed by Util.toCommand
    // though we could modify that to use getDeclaredConstructor and to instantiate its commands
    // with an explicit executable
    Command() {
        this("command", [], [])
    }

    Command(String executable) {
        this(executable, [], [])
    }

    Command(List<String> command) {
        this(command[0], command[ 1 .. command.size() - 1 ], [])
    }

    Command(String executable, List args) {
        this(executable, args, [])
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

    // XXX: squashed bug: static: needs to be available in a constructor e.g. before instance is available
    static protected List<String> cloneArgs(List<String> args) {
        new ArrayList<String>(args)
    }

    public boolean equals(Command other) {
        other.class == this.class &&
        other.executable == this.executable &&
        other.args == this.args &&
        other.output == this.output
    }

    public String toString() {
        "{ class: ${this.class.name}, executable: $executable, args: $args, output: $output }"
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

    private List<String> expandMacros(List<String> list, String uri) {
        list.collect { it.replace('URI', uri) }
    }

    protected List<String> toList(String uri) {
        [ executable ] + expandMacros(args, uri) + expandMacros(output, uri)
    }
}

@InheritConstructors
class Downloader extends Command {
    private List<String> expandMacros(List<String> list, String uri, String downloaderOut) {
        assert uri
        list.collect { it.replace('URI', uri).replace('DOWNLOADER_OUT', downloaderOut) }
    }

    protected List<String> toList(String uri, String downloaderOut) {
        [ executable ] + expandMacros(args, uri, downloaderOut) + expandMacros(output, uri, downloaderOut)
    }
}

@InheritConstructors
class Transcoder extends Command {
    private List<String> expandMacros(List<String> list, String uri, String transcoderOut) {
        assert uri
        list.collect { assert it; it.replace('URI', uri).replace('TRANSCODER_OUT', transcoderOut) }
    }

    protected List<String> toList(String uri, String transcoderOut) {
        [ executable ] + expandMacros(args, uri, transcoderOut) + expandMacros(output, uri, transcoderOut)
    }
}

@InheritConstructors
class MPlayer extends Downloader {
    static String defaultExecutable = Platform.MPLAYER_PATH
    private static List<String> defaultArgs = []

    MPlayer() {
        this(defaultExecutable, getDefaultArgs(), [])
    }

    MPlayer(List args) {
        this(defaultExecutable, args, [])
    }

    public static List<String> getDefaultArgs() {
        cloneArgs(this.defaultArgs)
    }

    public static List<String> setDefaultArgs(Object stringOrList) {
        this.defaultArgs = Util.toStringList(stringOrList)
    }
}

@InheritConstructors
class MEncoder extends Transcoder {
    static String defaultExecutable = Platform.MENCODER_PATH
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    MEncoder() {
        this(defaultExecutable, getDefaultArgs(), getDefaultOutputArgs())
    }

    MEncoder(List args) {
        this(defaultExecutable, args, getDefaultOutputArgs())
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
}

@InheritConstructors
class Ffmpeg extends Transcoder {
    static String defaultExecutable = Platform.FFMPEG_PATH
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    Ffmpeg() {
        this(defaultExecutable, getDefaultArgs(), getDefaultOutputArgs())
    }

    Ffmpeg(List args) {
        this(defaultExecutable, args, getDefaultOutputArgs())
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
}
