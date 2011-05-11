@Typed

package com.chocolatey.pmsencoder

import groovy.transform.*

// @InheritConstructors
class TestTranscoder extends Transcoder {
    TestTranscoder() {
        super('transcoder', [], [])
    }
}
