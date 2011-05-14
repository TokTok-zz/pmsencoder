import com.chocolatey.pmsencoder.MPlayer

/*
    navix://channel?url=http%3A//example.com&referer=http%3A//example.com&agent=Mozilla

    This protocol uses MPlayer as the downloader.
    Only the following Navi-X output fields are supported:

        url     // required: media URL
        agent   // optional: HTTP user-agent
        referer // optional: HTTP referrer
        player  // optional: currently ignored

    Although most fields are optional, there is no point using this protocol unless
    at least one optional field is supplied.

    boolean values (none currently) can be set without a value e.g. navix://channel?url=http%3A//example.com&foo
    values *must* be URL-encoded
    keys are just alphanumeric, so don't need to be
*/

init {
    profile ('navix://') {
        pattern {
            protocol 'navix'
        }

        action {
            def mplayerArgs = []
            def pairs = http.getNameValuePairs(uri) // uses URLDecoder.decode to decode the name and value
            def seenURL = false

            for (pair in pairs) {
                def name = pair.name
                def value = pair.value

                switch (name) {
                    case 'url':
                        if (value) {
                            // quote handling is built in for the URI
                            uri = value
                            seenURL = true
                        }
                        break
                    case 'referer':
                        if (value)
                            mplayerArgs << '-referrer' << quoteURI(value) // requires a recent (>= June 2010) mplayer
                        break
                    case 'agent':
                        if (value)
                            mplayerArgs << '-user-agent' << quoteURI(value)
                        break
                    case 'player':
                        if (value)
                            logger.info("player option for navix:// protocol currently ignored: ${value}")
                        break
                    default:
                        logger.warn("unsupported navix:// option: ${name}=${value}")
                }
            }

            if (seenURL) {
                downloader = new MPlayer()
                args (downloader.args) {
                    append(mplayerArgs)
                }
            } else {
                logger.error("invalid navix:// URI: no url parameter supplied: ${uri}")
            }
        }
    }
}
