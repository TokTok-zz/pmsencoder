@Typed
package com.chocolatey.pmsencoder

class Action {
    // FIXME: sigh: transitive delegation doesn't work (Groovy bug)
    // The order is important! Don't delegate to Matcher's propertyMissing
    // before the ProfileDelegate method
    @Delegate private final Response response
    @Delegate private final ProfileDelegate profileDelegate
    @Delegate private final Matcher matcher

    Action(ProfileDelegate profileDelegate) {
        this.profileDelegate = profileDelegate
        this.matcher = profileDelegate.matcher
        this.response = profileDelegate.response
    }

    // write these out separately to work around issues with delegation and default args
    private List<String> args(Closure closure) {
        args(response.transcoder.args, closure)
    }

    private List<String> args(List<String> args, Closure closure) {
        closure.delegate = new ArgsDelegate(args)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        args
    }

    // FIXME: no idea why I have to do this (ProfileDelegate exposes a public getter),
    // but browser is null in gameswelt without it
    public Browser getBrowser() {
        profileDelegate.getBrowser()
    }

    // define a variable in the response stash
    // DSL method
    void let(Map map) {
        map.each { key, value -> response.let(key, value) }
    }

    private Map<String, String> getFormatURLMap(String video_id) {
        def fmt_url_map = [:]

        def found = [ '&el=embedded', '&el=detailspage', '&el=vevo' , '' ].any { String param ->
            def uri = "http://www.youtube.com/get_video_info?video_id=${video_id}${param}&ps=default&eurl=&gl=US&hl=en"
            def regex = '\\bfmt_url_map=(?<youtube_fmt_url_map>[^&]+)'
            def newStash = new Stash()
            def document = http.get(uri)

            if ((document != null) && RegexHelper.match(document, regex, newStash)) {
                // XXX type-inference fail
                List<String> fmt_url_pairs = URLDecoder.decode(newStash['youtube_fmt_url_map']).tokenize(',')
                fmt_url_pairs.inject(fmt_url_map) { Map<String, String> map, String pair ->
                    // XXX type-inference fail
                    List<String> kv = pair.tokenize('|')
                    map[kv[0]] = kv[1]
                    return map
                }
                return true
            } else {
                return false
            }
        }

        return found ? fmt_url_map : null
    }

    // DSL method
    void youtube(List<Integer> formats = youtubeAccept) {
        def uri = response['uri']
        def video_id = response['youtube_video_id']
        def t = response['youtube_t']
        def found = false

        assert video_id != null
        assert t != null

        response.let('youtube_uri', uri)

        if (formats.size() > 0) {
            def fmt_url_map = getFormatURLMap(video_id)
            if (fmt_url_map != null) {
                logger.trace('fmt_url_map: ' + fmt_url_map)

                found = formats.any { fmt ->
                    def fmtString = fmt.toString()
                    logger.debug("checking fmt_url_map for $fmtString")
                    def stream_uri = fmt_url_map[fmtString]

                    if (stream_uri != null) {
                        // set the new URI
                        logger.debug('success')
                        response.let('youtube_fmt', fmtString)
                        response.let('uri', stream_uri)
                        return true
                    } else {
                        logger.debug('failure')
                        return false
                    }
                }
            } else {
                logger.fatal("can't find fmt -> URI map in video metadata")
            }
        }  else {
            logger.fatal("no formats defined for $uri")
        }

        if (!found) {
            logger.fatal("can't retrieve stream URI for $uri")
        }
    }
}
