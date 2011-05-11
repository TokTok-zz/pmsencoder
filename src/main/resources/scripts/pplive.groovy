import com.chocolatey.pmsencoder.MEncoder

script {
    profile ('PPLive') {
        pattern {
            protocol 'synacast'
            match { PPLIVE }
        }

        action {
            hook = [ PPLIVE, 'URI' ]
            uri = PPLIVE_URI ?: 'http://127.0.0.1:8888'
            // see sopcast.groovy
            transcoder = new MEncoder()
        }
    }
}
