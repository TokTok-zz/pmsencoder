// videofeed.Web,Test,GameTrailers=http://www.gametrailers.com/rssgenerate.php?s1=&favplats[ps3]=ps3&quality[hd]=on&agegate[no]=on&orderby=newest&limit=100
script {
    profile ('GameTrailers') {
        pattern {
            domain 'gametrailers.com'
        }

        action {
            def gmi = scrape('\\bmov_game_id\\s*=\\s*(?<gametrailers_movie_id>\\d+)')
            def gfl = scrape('\\bhttp://www\\.gametrailers\\.com/download/\\d+/(?<gametrailers_filename>t_[^.]+)\\.wmv\\b')

            if (gmi && gfl) {
                uri = "http://trailers-ak.gametrailers.com/gt_vault/${gametrailers_movie_id}/${gametrailers_filename}.flv"
            } else if (scrape('\\bvar\\s+mov_id\\s*=\\s*(?<gametrailers_mov_id>\\d+)')) {
                def scrapeURI = "http://www.gametrailers.com/neo/?page=xml.mediaplayer.Mediagen&movieId=${gametrailers_mov_id}&hd=1"
                scrape '<src>\\s*(?<uri>\\S+)\\s*</src>', [ uri: scrapeURI ]
            }
        }
    }
}
