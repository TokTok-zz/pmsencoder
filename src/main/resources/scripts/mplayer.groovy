end {
    profile ('MPlayer') {
        pattern {
            match { !downloader }
            match {
                // http://www.ffmpeg.org/ffmpeg-doc.html#SEC33
                protocol && !(protocol in [
                    'concat',
                    'file',
                    'gopher',
                    'http',
                    'pipe',
                    'rtmp',
                    'rtmpe',
                    'rtmps',
                    'rtmpt',
                    'rtmpte',
                    'rtp',
                    'tcp',
                    'udp'
                ])
            }
        }

        action {
            // don't clobber MEncoder options if they've already been set
            if (!(transcoder instanceof MEncoder))
                transcoder = $mencoder
        }
    }
}
