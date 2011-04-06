script {
    profile ('Args') {
        action {
            hook = 'hook -foo -bar -baz'
        }
    }

    profile ('Args Set') {
        pattern {
            domain 'args.set.com'
        }

        action {
            args(hook.args) { set '-quux' }
            args(downloader.args) { set '-bar' }
            args(transcoder.args) { set '-bar' }
            args { set '-bar' }
        }
    }

    profile ('Args Remove') {
        pattern {
            domain 'args.remove.com'
        }

        action {
            args(hook.args) { remove '-bar' }
            args(downloader.args) { remove '-bar' }
            args(transcoder.args) { remove '-bar' }
            args(transcoder.output) { remove '-bar' }
        }
    }
}
