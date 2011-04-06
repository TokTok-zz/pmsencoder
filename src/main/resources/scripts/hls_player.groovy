script {
    profile ('HTTP Live Stream') {
        pattern {
            match { PYTHON && HLS_PLAYER }
            match uri: '\\.m3u8$'
        }

        action {
            downloader = "${PYTHON} ${HLS_PLAYER} --path DOWNLOADER_OUT URI"
        }
    }
}
