script {
    profile ('file://') {
        pattern {
            protocol 'file'
            match { protocol == 'file' }
            match { protocol != 'http' }
        }

        action {
            args {
                set '-protocol': protocol
            }
        }
    }

    profile ('http://') {
        pattern {
            protocol 'http'
            match { protocol == 'http' }
            match { protocol != 'file' }
        }

        action {
            args {
                set '-protocol': protocol
            }
        }
    }
}
