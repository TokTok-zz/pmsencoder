script {
    profile ('jsoup') {
        pattern {
            domain 'eurogamer.net'

            // confirm that it works in the pattern block
            match {
                $('title').text() == 'Uncharted 3 chateau gameplay part 2 - Eurogamer Videos | Eurogamer.net'
            }
        }

        action {
            // confirm that it works in the action block
            uri = 'http://www.eurogamer.net/' + $('a.download').attr('href')
        }
    }
}
