script {
    profile ('Args Set') {
        pattern {
            domain 'args.set.com'
        }

        action {
            args(hook.args) { set '-baz' }
            args(downloader.args) { set '-baz' }
            args(transcoder.output) { set '-baz' }
            args(transcoder.args) { set '-baz' }
            args { set '-quux' }
        }
    }

    profile ('Args Remove') {
        pattern {
            domain 'args.remove.com'
        }

        action {
            args(hook.args) { remove '-foo' }
            args(downloader.args) { remove '-foo' }
            args(transcoder.output) { remove '-foo' }
            args(transcoder.args) { remove '-foo' }
            args { remove '-bar' }
        }
    }
}
