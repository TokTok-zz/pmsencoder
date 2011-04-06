// replace the GameTrailers profile
script {
    profile ('GameTrailers Replacement', replaces: 'GameTrailers') {
        pattern {
            domain 'gametrailers.com'
        }

        action {
            args {
                set '-gametrailers': 'replacement'
            }
        }
    }
}
