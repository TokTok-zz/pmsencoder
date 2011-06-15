import com.chocolatey.pmsencoder.command.MPlayer

// this needs to precede 'YouTube Metadata' (check), hence: script (see, for now, TODO.groovy for stages)

script {
    def ICHC = 'I Can Has Cheezburger'

    profile (ICHC) {
        pattern {
            domain 'icanhascheezburger.com'
        }
    }

    profile ('I Can Has YouTube') {
        pattern {
            match ICHC
            scrape '\\bhttp://www\\.youtube\\.com/v/(?<video_id>[^&?]+)'
        }

        action {
            uri = "http://www.youtube.com/watch?v=${video_id}"
        }
    }

    profile ('I Can Has Viddler') {
        pattern {
            match ICHC
            match { IPAD_USER_AGENT }
            scrape "\\bsrc='(?<uri>http://www\\.viddler\\.com/file/\\w+/html5mobile/)'"
        }

        action {
            downloader = new MPlayer()
            args (downloader.args) { set '-user-agent': IPAD_USER_AGENT }
        }
    }
}
