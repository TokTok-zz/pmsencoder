script {
    def transcoder = '/usr/bin/transcoder'

    profile ('Transcoder List') {
        pattern {
            domain 'transcoder-list.com'
        }

        action {
            transcoder = [ transcoder, 'list', uri ]
        }
    }

    profile ('Transcoder String') {
        pattern {
            domain 'transcoder-string.com'
        }

        action {
            transcoder = "$transcoder string $uri"
        }
    }
}
