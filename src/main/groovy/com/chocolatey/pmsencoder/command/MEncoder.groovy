@Typed

package com.chocolatey.pmsencoder.command

import groovy.transform.*

@InheritConstructors
class MEncoder extends Transcoder {
    static String defaultExecutable = Platform.MENCODER_PATH
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    MEncoder() {
        this(defaultExecutable, getDefaultArgs(), getDefaultOutputArgs())
    }

    @Override
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
