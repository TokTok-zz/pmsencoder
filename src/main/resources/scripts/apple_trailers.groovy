import com.chocolatey.pmsencoder.command.MPlayer

script {
    profile ('Apple Trailers') {
        pattern {
            match uri: '^http://(?:(?:movies|www|trailers)\\.)?apple\\.com/.+$'
        }

        action {
            downloader = new MPlayer()

            args (downloader.args) {
                set '-user-agent': 'QuickTime/7.6.2'
            }
        }
    }
}
