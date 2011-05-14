import com.chocolatey.pmsencoder.Util

// videofeed.Web,Gameswelt=http://www.gameswelt.de/feeds/videos/rss.xml

script {
    profile ('Gameswelt') {
        pattern {
            match uri: '^http://www\\.gameswelt\\.de/videos/'
        }

        // extract the video URI e.g.
        // HD: http://video.gameswelt.de/public/mp4/201105/40353_geforcegtx560launchtrailer_1_HD.mp4
        // SD: http://video.gameswelt.de/public/mp4/201105/40353_geforcegtx560launchtrailer_1.mp4
        action {
            def hdUri = $('span.videoHD > a').attr('href')

            if (hdUri) { // TODO: handle SD if no HD vid is available
                use (Util) { // for String.match()
                    def path = $(uri: hdUri)('div#boxRight a').first().attr('onmouseover').match("'(\\d+\\/\\d+[^']+)'")[1]
                    uri = "http://video.gameswelt.de/public/mp4/${path}"
                }
            }
        }
    }
}
