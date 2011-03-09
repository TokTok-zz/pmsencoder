/*
    navix://channel?url=http%3A//example.com&referer=http%3A//example.com&agent=Mozilla

    This protocol uses MEncoder as the downloader/transcoder
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
            def mencoderArgs = []
            def pairs = $HTTP.getNameValuePairs($URI) // uses URLDecoder.decode to decode the name and value
            def seenURL = false

            for (pair in pairs) {
                def name = pair.name
                def value = pair.value

                switch (name) {
                    case 'url':
                        if (value) {
                            // quote handling is built in for MEncoder
                            $URI = value
                            seenURL = true
                        }
                        break
                    case 'referer':
                        if (value)
                            mencoderArgs << '-referrer' << quoteURI(value) // requires a recent (>= June 2010) mplayer
                        break
                    case 'agent':
                        if (value)
                            mencoderArgs << '-user-agent' << quoteURI(value)
                        break
                    case 'player':
                        if (value)
                            log.info("player option for navix:// protocol currently ignored: ${value}")
                        break
                    default:
                        log.warn("unsupported navix:// option: ${name}=${value}")
                }
            }

            if (seenURL) {
                $TRANSCODER = $MENCODER + mencoderArgs
            } else {
                log.error("invalid navix:// URI: no url parameter supplied: ${$URI}")
            }
        }
    }
}
