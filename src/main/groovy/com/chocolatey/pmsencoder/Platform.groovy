@Typed
package com.chocolatey.pmsencoder

import com.sun.jna.Platform as JPlatform

public class Platform {
    private static final String platform
    private static final String extension

    static {
        if (JPlatform.isWindows()) {
            platform = 'win32'
            extension = '.exe'
        } else {
            extension = ''

            if (JPlatform.isMac()) {
                platform = 'osx'
            } else {
                platform = 'linux' // XXX - not sure if/where e.g. tsMuxeR is bundled on other Unices
            }
        }
    }

    public static String getExecutable(String name) {
        def file = new File(platform, name + extension);
        return (file.exists() && file.isFile()) ? file.path : null
    }
}
