script {
    profile ('Scrape String') {
        pattern {
            domain 'scrape.string'
        }

        action {
            // scraping from a string
            scrape (source: 'scrape string')('^(?<first>\\w+)\\s+(?<second>\\w+$)')
        }
    }
}
