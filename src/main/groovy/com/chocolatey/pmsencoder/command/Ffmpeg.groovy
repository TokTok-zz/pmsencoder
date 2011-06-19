@Typed

package com.chocolatey.pmsencoder.command

import com.chocolatey.pmsencoder.*

import groovy.transform.*

@InheritConstructors
class Ffmpeg extends Transcoder {
    static String defaultExecutable = Platform.FFMPEG_PATH
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    Ffmpeg() {
        this(defaultExecutable, getDefaultArgs(), getDefaultOutputArgs())
    }

    @Override
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
