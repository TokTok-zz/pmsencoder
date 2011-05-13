import com.chocolatey.pmsencoder.MEncoder

// videofeed.Web,Eurogamer=http://rss.feedsportal.com/feed/eurogamer/eurogamer_tv

script {
    profile ('Eurogamer') {
        pattern {
            domain 'eurogamer.net'
        }

        action {
            // FIXME: temporary while MPlayer doesn't work as a downloader on Windows
            transcoder = new MEncoder()
            // -referrer requires a recent-ish MEncoder (>= June 2010)
            args { set '-referrer': uri }
            uri = 'http://www.eurogamer.net/' + jQuery('$("a.download").attr("href")')
        }
    }
}
