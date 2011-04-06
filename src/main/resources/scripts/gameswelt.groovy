// videofeed.Web,Gameswelt=http://www.gameswelt.de/feeds/videos/rss.xml

script {
   profile ('Gameswelt') {
        pattern {
            match uri: '^http://www\\.gameswelt\\.de/videos/'
        }

        action {
            // extract the video URI from the value of the flashvars param
            scrape '<param\\s+name="flashvars".+(?<uri>http://video\\.gameswelt\\.de/[^&]+)'
        }
    }
}
