script {
    profile ('Apple Trailers') {
        pattern {
            match uri: '^http://(?:(?:movies|www|trailers)\\.)?apple\\.com/.+$'
        }

        action {
            // FIXME: temporary while MPlayer doesn't work as a downloader on Windows
            transcoder = $mencoder

            args {
                set '-user-agent': 'QuickTime/7.6.2'
            }
        }
    }
}
