import com.chocolatey.pmsencoder.command.MEncoder
import com.chocolatey.pmsencoder.command.MPlayer

// if ffmpeg can't handle the protocol, use MPlayer as the downloader
end {
    profile ('MPlayer') {
        pattern {
            // don't use MPlayer if a downloader has already been set
            // don't use MPlayer if MEncoder is already being used to transcode
            match { !downloader }
            match { !(transcoder instanceof MEncoder) } // FIXME: bring back reject { ... }
            match {
                // http://www.ffmpeg.org/ffmpeg-doc.html#SEC33
                protocol && !(protocol in [
                    'concat',
                    'file',
                    'gopher',
                    'http', // XXX: https?
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
            downloader = new MPlayer()
        }
    }
}
