@Typed
package com.chocolatey.pmsencoder

import net.pms.dlna.DLNAResource
import net.pms.io.OutputParams

class Request {
    final String uri
    final DLNAResource dlna
    final OutputParams params

    // XXX: can't get the named params initializer to work with final fields
    // e.g. new Request(dlna: dlna, params: params, uri: uri)
    // it's moot anyway as we need a copy constructor
    Request(String uri, DLNAResource dlna, OutputParams params) {
        this.uri = uri
        this.dlna = dlna
        this.params = params
    }

    // copy constructor - all fields are immutable so we can just copy them
    Request(Request other) {
        this.uri = other.uri
        this.dlna = other.dlna
        this.params = other.params
    }
}
