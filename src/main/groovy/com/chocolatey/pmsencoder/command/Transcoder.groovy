@Typed

package com.chocolatey.pmsencoder.command

import groovy.transform.*

@InheritConstructors
class Transcoder extends Command {
    private List<String> expandMacros(List<String> list, String uri, String transcoderOut) {
        assert uri
        list.collect { assert it; it.replace('URI', uri).replace('TRANSCODER_OUT', transcoderOut) }
    }

    protected List<String> toList(String uri, String transcoderOut) {
        [ executable ] + expandMacros(args, uri, transcoderOut) + expandMacros(output, uri, transcoderOut)
    }
}
