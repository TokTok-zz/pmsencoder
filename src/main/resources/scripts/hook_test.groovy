script {
    profile ('Example Hook') {
        pattern {
            match { NOTIFY_SEND != NULL }
        }

        action {
            def name = request.dlna.getName()
            hook = [ NOTIFY_SEND, 'PMSEncoder', "playing ${name}" ]
        }
    }
}
