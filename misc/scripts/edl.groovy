import com.chocolatey.pmsencoder.Util
import net.pms.util.FileUtil

script {
    profile ('EDL', engine: 'mencoder') {
        pattern {
            def filename = new File(uri).path
            edl = FileUtil.getFileNameWithoutExtension(filename) + '.edl'
            match { Util.fileExists(edl) }
        }

        action {
            args {
                set '-edl': edl
            }
        }
    }
}
