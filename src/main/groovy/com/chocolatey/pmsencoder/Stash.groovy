@Typed
package com.chocolatey.pmsencoder

import java.lang.String as JString

// a long-winded way of getting Java Strings and Groovy GStrings to play nice
class Stash extends LinkedHashMap<JString, JString> {
    public Stash() {
        super()
    }

    public Stash(Stash other) {
        super()
        def self = this
        other.each { key, value -> self[ key?.toString() ] = value?.toString() }
    }

    public Stash(Map map) {
        def self = this
        map.each { key, value -> self[ key?.toString() ] = value?.toString() }
    }

    public JString getAt(Object key) {
        this.get(key?.toString())
    }

    public JString putAt(Object key, Object value) {
        this.put(key?.toString(), value?.toString())
    }
}
