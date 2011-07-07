@Typed

package com.chocolatey.pmsencoder.command

import com.chocolatey.pmsencoder.*

import groovy.transform.*

@InheritConstructors
class Downloader extends Command {
    private List<String> expandMacros(List<String> list, String uri, String downloaderOut) {
        assert uri
        list.collect { it.replace('URI', uri).replace('DOWNLOADER_OUT', downloaderOut) }
    }

    public List<String> toList(String uri, String downloaderOut) {
        [ executable ] + expandMacros(args, uri, downloaderOut) + expandMacros(output, uri, downloaderOut)
    }
}
