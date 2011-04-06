script {
    def downloader = '/usr/bin/downloader'

    profile ('Downloader List') {
        pattern {
            domain 'downloader-list.com'
        }

        action {
            downloader = [ downloader, 'list', uri ]
        }
    }

    profile ('Downloader String') {
        pattern {
            domain 'downloader-string.com'
        }

        action {
            downloader = "$downloader string $uri"
        }
    }
}
