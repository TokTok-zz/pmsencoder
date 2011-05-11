script {
    // XXX: squashed bug: careful not to shadow the field
    // i.e. don't call a variable downloader, hook, transcoder &c.
    // likewise: downloader.groovy -> downloader_test.groovy to avoid:
    // "you tried to assign a value to the class 'downloader'" &c. errors
    def downloader_path = '/usr/bin/downloader'

    profile ('Downloader List') {
        pattern {
            domain 'downloader-list.com'
        }

        action {
            downloader = [ downloader_path, 'list', uri ]
        }
    }

    profile ('Downloader String') {
        pattern {
            domain 'downloader-string.com'
        }

        action {
            downloader = "$downloader_path string $uri"
        }
    }
}
