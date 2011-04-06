// videofeed.Web,Eurogamer=http://rss.feedsportal.com/feed/eurogamer/eurogamer_tv

script {
    profile ('Eurogamer') {
        pattern {
            domain 'eurogamer.net'
        }

        action {
            // FIXME: temporary while MPlayer doesn't work as a downloader on Windows
            transcoder = $mencoder
            args {
                set '-referrer': uri // -referrer requires a recent-ish MEncoder (>= June 2010)
            }
            uri = 'http://www.eurogamer.net/' + browse { $('a.download').@href }
        }
    }
}
