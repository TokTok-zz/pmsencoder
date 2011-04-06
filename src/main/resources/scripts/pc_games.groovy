// videofeed.Web,PCGames=http://videos.pcgames.de/rss/newest.php

script {
    profile ('PC Games') {
        pattern {
            domain 'pcgames.de'
        }

        action {
            uri = http.target(uri)
            scrape '\\bcurrentposition\\s*=\\s*(?<currentPosition>\\d+)\\s*;'
            scrape "'(?<uri>http://\\w+\\.gamereport\\.de/videos/[^']+?/${currentPosition}/[^']+)'"
        }
    }
}
