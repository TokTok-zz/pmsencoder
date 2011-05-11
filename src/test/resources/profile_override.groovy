// override the GameTrailers profile
script {
    profile ('GameTrailers') {
        pattern {
            domain 'gametrailers.com'
        }

        action {
            args {
                set '-game': 'trailers'
            }
        }
    }
}
