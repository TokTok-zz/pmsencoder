// videofeed.Web,Wimp=http://www.wimp.com/rss/

script {
    profile ('Get Flash Videos') {
        pattern {
            match { PERL && GET_FLASH_VIDEOS }
            domains([ 'wimp.com', 'megavideo.com' ]) // &c.
        }

        action {
            downloader = "${PERL} ${GET_FLASH_VIDEOS} --quality high --quiet --yes --filename DOWNLOADER_OUT URI"
        }
    }
}
