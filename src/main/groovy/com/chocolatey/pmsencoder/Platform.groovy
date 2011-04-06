@Typed
package com.chocolatey.pmsencoder

import com.sun.jna.Platform as JPlatform

import net.pms.PMS

public class Platform {
    private static final String PLATFORM
    private static final String EXTENSION
    public static final String FFMPEG_PATH = PMS.getConfiguration().getFfmpegPath()
    public static final String MENCODER_PATH = PMS.getConfiguration().getMencoderPath()
    public static final String MENCODER_MT_PATH = PMS.getConfiguration().getMencoderMTPath()
    public static final String MPLAYER_PATH = PMS.getConfiguration().getMplayerPath()
    public static final String VLC_PATH = PMS.getConfiguration().getVlcPath()
    public static final String OS = System.getProperty('os.name')

    static {
        if (JPlatform.isWindows()) {
            PLATFORM = 'win32'
            EXTENSION = '.exe'
        } else {
            EXTENSION = ''

            if (JPlatform.isMac()) {
                PLATFORM = 'osx'
            } else {
                PLATFORM = 'linux' // XXX - not sure if/where e.g. tsMuxeR is bundled on other Unices
            }
        }
    }

    public static String getExecutable(String name) {
        def file = new File(PLATFORM, name + EXTENSION);
        return Util.fileExists(file) ? file.path : null
    }

    public static boolean isWindows() {
        JPlatform.isWindows()
    }
}
