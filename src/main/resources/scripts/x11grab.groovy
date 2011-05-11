// videostream.Web,Screen=Screen,x11grab://

script {
    profile ('x11grab://') {
        pattern {
            match { !pms.isWindows() }
            match uri: '^x11grab://(?<display>.*)$'
        }

        action {
            if (!display)
                display = ':0.0'

            uri = display

            args {
                set '-f': 'x11grab'
                // this needs to be configurable via the URI
                set '-s': '1024x768'
            }
        }
    }
}
