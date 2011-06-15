@Typed

package com.chocolatey.pmsencoder.command

import com.chocolatey.pmsencoder.command.Transcoder

import groovy.transform.*

// @InheritConstructors
class TestTranscoder extends Transcoder {
    TestTranscoder() {
        super('transcoder', [], [])
    }
}
