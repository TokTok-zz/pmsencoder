import com.chocolatey.pmsencoder.command.MPlayer

// videofeed.Web,Eurogamer=http://rss.feedsportal.com/feed/eurogamer/eurogamer_tv

script {
    profile ('Eurogamer') {
        pattern {
            domain 'eurogamer.net'
        }

        action {
            downloader = new MPlayer()
            // -referrer requires a recent-ish MPlayer (>= June 2010)
            args (downloader.args) { set '-referrer': uri }
            uri = 'http://www.eurogamer.net/' + $('a.download').attr('href')
        }
    }
}
