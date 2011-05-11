script {
    def transcoder_path = '/usr/bin/transcoder'

    profile ('Transcoder List') {
        pattern {
            domain 'transcoder-list.com'
        }

        action {
            transcoder = [ transcoder_path, 'list', uri ]
        }
    }

    profile ('Transcoder String') {
        pattern {
            domain 'transcoder-string.com'
        }

        action {
            transcoder = "$transcoder_path string $uri"
        }
    }
}
