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
            def getFields = { string -> Util.extract(string, "\\('?(\\d+)'?\\s*,\\s*'(\\d+)\\/([^']+)'") }
            def hdUri = jQuery('$("span.videoHD > a").attr("href")')
            def id1, id2, filename

            if (hdUri) {
                browser.navigate(hdUri)
                (id1, id2, filename) = getFields(jQuery('$("a.downloadLink").attr("onclick")'))
            } else {
                (id1, id2, filename) = getFields(jQuery('$("span.videoDownload").attr("href")'))
            }

            uri = sprintf('http://video.gameswelt.de/public/mp4/%s/%s_%s', id2, id1, filename)
        }
    }
}
