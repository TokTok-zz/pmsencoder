@Typed

package com.chocolatey.pmsencoder.command

import com.chocolatey.pmsencoder.*

import groovy.transform.*

@InheritConstructors
class MPlayer extends Downloader {
    static String defaultExecutable = Platform.MPLAYER_PATH
    private static List<String> defaultArgs = []

    MPlayer() {
        super(defaultExecutable, getDefaultArgs(), [])
    }

    MPlayer(List args) {
        super(defaultExecutable, args, [])
    }

    public static List<String> getDefaultArgs() {
        cloneArgs(this.defaultArgs)
    }

    public static List<String> setDefaultArgs(Object stringOrList) {
        this.defaultArgs = Util.toStringList(stringOrList)
    }
}
