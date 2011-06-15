@Typed
package com.chocolatey.pmsencoder

import com.chocolatey.pmsencoder.command.Transcoder

import net.pms.dlna.DLNAMediaInfo
import net.pms.dlna.DLNAResource
import net.pms.io.OutputParams

class Request {
    final String engine
    final String uri
    final DLNAResource dlna
    final DLNAMediaInfo media
    final OutputParams params
    final Transcoder transcoder

    // XXX: can't get the named params initializer to work with final fields
    // e.g. new Request(dlna: dlna, params: params, uri: uri)
    // it's moot anyway as we need a copy constructor

    Request(
        String engine,
        String uri,
        DLNAResource dlna,
        DLNAMediaInfo media,
        OutputParams params,
        Transcoder transcoder = null
    ) {
        this.engine = engine
        this.uri = uri
        this.dlna = dlna
        this.media = media
        this.params = params
        this.transcoder = transcoder
    }

    // copy constructor - all fields are immutable so we can just copy them
    Request(Request other) {
        this.engine = other.engine
        this.uri = other.uri
        this.dlna = other.dlna
        this.media = other.media
        this.params = other.params
        this.transcoder = other.transcoder
    }
}
