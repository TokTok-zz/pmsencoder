script {
    profile ('Example Hook') {
        pattern {
            match { NOTIFY_SEND != NULL }
        }

        action {
            // FIXME: use the title
            hook = [ NOTIFY_SEND, 'PMSEncoder', "playing ${uri}" ]
        }
    }
}
