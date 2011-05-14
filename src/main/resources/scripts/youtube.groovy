// videofeed.Web,YouTube=http://gdata.youtube.com/feeds/base/users/freddiew/uploads?alt=rss&v=2&orderby=published

check {
    // extract metadata about the video for other profiles
    profile ('YouTube Metadata') {
        // extract the resource's video_id from the URI of the standard YouTube page
        pattern {
            match uri: '^http://(?:\\w+\\.)?youtube\\.com/watch\\?v=(?<youtube_video_id>[^&]+)'
        }

        action {
            // fix the URI to bypass age verification
            def youtube_scrape_uri = "${uri}&has_verified=1"

            // extract the resource's sekrit identifier (t) from the HTML
            scrape (uri: youtube_scrape_uri)('\\bflashvars\\s*=\\s*["\'][^"\']*?\\bt=(?<youtube_t>[^&"\']+)')

            // extract the title and uploader ("creator") so that scripts can use them
            def $j = $(uri: youtube_scrape_uri) // curry
            youtube_title = $j('meta[name=title]').attr('content')
            youtube_uploader = $j('span[data-subscription-type=user]').attr('data-subscription-username')
        }
    }

    profile ('YouTube-DL Compatible') {
        pattern {
            // match any of the sites youtube-dl supports - copied from the source
            match uri: [
                '^(?:http://)?(?:[a-z0-9]+\\.)?photobucket\\.com/.*[\\?\\&]current=(.*\\.flv)',
                '^(?:http://)?(?:[a-z]+\\.)?video\\.yahoo\\.com/(?:watch|network)/([0-9]+)(?:/|\\?v=)([0-9]+)(?:[#\\?].*)?',
                '^(?:https?://)?(?:\\w+\\.)?facebook.com/video/video.php\\?(?:.*?)v=(?P<ID>\\d+)(?:.*)',
                '^((?:https?://)?(?:youtu\\.be/|(?:\\w+\\.)?youtube(?:-nocookie)?\\.com/)(?:(?:(?:v|embed)/)|(?:(?:watch(?:_popup)?(?:\\.php)?)?(?:\\?|#!?)(?:.+&)?v=)))?([0-9A-Za-z_-]+)(?(1).+)?$',
                '^(?:http://)?video\\.google\\.(?:com(?:\\.au)?|co\\.(?:uk|jp|kr|cr)|ca|de|es|fr|it|nl|pl)/videoplay\\?docid=([^\\&]+).*',
                '^(?:http://)?(?:\\w+\\.)?depositfiles.com/(?:../(?#locale))?files/(.+)',
                '^(?:http://)?(?:www\\.)?metacafe\\.com/watch/([^/]+)/([^/]+)/.*',
                '^(?:(?:(?:http://)?(?:\\w+\\.)?youtube.com/user/)|ytuser:)([A-Za-z0-9_-]+)',
                '^(?:http://)?(?:\\w+\\.)?youtube.com/(?:(?:view_play_list|my_playlists|artist)\\?.*?(p|a)=|user/.*?/user/|p/|user/.*?#[pg]/c/)([0-9A-Za-z]+)(?:/.*?/([0-9A-Za-z_-]+))?.*',
                '^(?i)(?:https?://)?(?:www\\.)?dailymotion\\.[a-z]{2,3}/video/([^_/]+)_([^/]+)'
            ]
        }

        action {
            // XXX: keep this up-to-date
            youtube_dl_compatible = '2011.02.25c' // version the regexes were copied from
        }
    }

    // perform the actual YouTube handling if the metadata has been extracted.
    // separating the profiles into metadata and implementation allows scripts to
    // override just this profile without having to rescrape the page to match on
    // the uploader &c.
    //
    // it also simplifies custom matchers e.g. check for 'YouTube Medatata' in matches
    // rather than repeating the regex

    profile ('YouTube-DL') {
        pattern {
            match 'YouTube-DL Compatible'
            match { YOUTUBE_DL }
        }

        action {
            youtube_dl_enabled = true
            // allow youtube-dl.exe to be used on Windows (or chmod +x youtube-dl on *nix)
            def python = PYTHON ? "$PYTHON " : ''

            if (YOUTUBE_DL_MAX_QUALITY) {
                downloader = "${python}${YOUTUBE_DL} --max-quality ${YOUTUBE_DL_MAX_QUALITY} -o DOWNLOADER_OUT URI"
            } else {
                downloader = "${python}${YOUTUBE_DL} -o DOWNLOADER_OUT URI"
            }
        }
    }

    profile ('YouTube') {
        pattern {
            // fall back to the native handler if youtube-dl is not installed/enabled
            match { youtube_video_id && !youtube_dl_enabled }
        }

        // Now, with video_id and t defined, call the builtin YouTube handler.
        // Note: the parentheses are required for a no-arg method call
        action {
            youtube()
        }
    }
}
