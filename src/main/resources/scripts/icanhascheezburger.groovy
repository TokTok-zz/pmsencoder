// this needs to precede 'YouTube Metadata' i.e. it needs to be in a BEGIN script

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
            // FIXME: temporary while MPlayer doesn't work as a downloader on Windows
            transcoder = $mencoder
            args { set '-user-agent': IPAD_USER_AGENT }
        }
    }
}
