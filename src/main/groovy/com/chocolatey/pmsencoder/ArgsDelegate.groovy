@Typed
package com.chocolatey.pmsencoder

class ArgsDelegate implements LoggerMixin {
    List<String> args

    ArgsDelegate(List<String> args) {
        this.args = args
    }

    private boolean isOption(String arg) {
        // a rare use case for Groovy's annoyingly lax definition of false.
        // and it's not really a use case because it requires three lines of
        // explanation: null and an empty string evaluate as false

        if (arg) {
           return arg ==~ /^-[^0-9].*$/
        } else {
            return false
        }
    }

    // set an option - create it if it doesn't exist
    private void setArg(Object name, Object value = null) {
        assert name != null

        def nameStr = name.toString()
        def index = args.findIndexOf { it == nameStr }

        if (index == -1) {
            if (value != null) {
                logger.debug("adding $name $value")
                /*
                    XXX squashed bug: careful not to perform operations on copies of this list
                    i.e. make sure it's modified in place:
                */
                args << nameStr << value.toString()
            } else {
                logger.debug("adding $name")
                args << nameStr
            }
        } else if (value != null) {
            logger.debug("setting $name to $value")
            args[ index + 1 ] = value.toString()
        }
    }

    void set(Map map) {
        // the sort order is predictable (for tests) as long as we (and Groovy) use LinkedHashMap
        map.each { name, value -> setArg(name, value) }
    }

    void set(Object name) {
        setArg(name)
    }

    // append a single option to the current argument list
    List<String> append(Object object) {
        args << object.toString()
        args
    }

    List<String> append(List list) {
        args.addAll(list*.toString())
        args
    }

    // prepend a single option to the current argument list
    List<String> prepend(Object object) {
        args.addAll(0, object.toString())
        args
    }

    List<String> prepend(List list) {
        args.addAll(0, list*.toString())
        args
    }

    // remove multiple option names and their corresponding values if they have one
    List<String> remove(List optionNames) {
        optionNames.each { remove(it) }
        args
    }

    // remove a single option name and its corresponding value if it has one
    List<String> remove(Object optionName) {
        assert optionName != null

        def optionNameStr = optionName.toString()
        def index = args.findIndexOf { it == optionNameStr }

        if (index >= 0) {
            def lastIndex = args.size() - 1
            def nextIndex = index + 1

            if (nextIndex <= lastIndex) {
                def nextArg = args[ nextIndex ]

                if (isOption(nextArg)) {
                    logger.debug("removing: $optionNameStr")
                } else {
                    logger.debug("removing: $optionNameStr $nextArg")
                    args.remove(nextIndex) // remove this first so the index is preserved for the option name
                }
            }

            args.remove(index)
        }

        return args
    }

    /*
        perform a string search-and-replace in the value of a transcoder option.
    */

    void replace(Map<Object, Map> replaceMap) {
        // the sort order is predictable (for tests) as long as we (and Groovy) use LinkedHashMap
        replaceMap.each { name, map ->
            // squashed bug (see  above): take care to modify args in-place
            def nameStr = name.toString()
            def index = args.findIndexOf { it == nameStr }

            if (index != -1) {
                map.each { search, replace ->
                    if ((index + 1) < args.size()) {
                        // TODO support named captures
                        logger.debug("replacing $search with $replace in $name")
                        def value = args[ index + 1 ]
                        // XXX squashed bug: strings are immutable!
                        args[ index + 1 ] = value.replaceAll(search.toString(), replace.toString())
                    } else {
                        logger.warn("can't replace $search with $replace in $name: target out of bounds")
                    }
                }
            }
        }
    }
}
