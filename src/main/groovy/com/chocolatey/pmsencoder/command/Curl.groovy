@Typed

package com.chocolatey.pmsencoder.command

import com.chocolatey.pmsencoder.*

import groovy.transform.*

@InheritConstructors
class Curl extends Downloader {
    static String defaultExecutable
    private static List<String> defaultArgs = []
    private static List<String> defaultOutputArgs = []

    Curl() {
        super(defaultExecutable, getDefaultArgs(), getDefaultOutputArgs())
    }

    Curl(List args) {
        super(defaultExecutable, args, getDefaultOutputArgs())
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
