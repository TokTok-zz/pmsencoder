@Typed
package com.chocolatey.pmsencoder

import net.pms.PMS

public class Util {
    public static List<String> scalarList(Object scalarOrList) {
        if (scalarOrList == null) {
            return []
        } else {
            return (scalarOrList instanceof List) ? scalarOrList.collect { it.toString() } : [ scalarOrList.toString() ]
        }
    }

    // allow args lists to be assigned via a String or List
    public static List<String> toStringList(Object stringOrList) {
        if (stringOrList == null) {
            null
        } else if (stringOrList instanceof List) {
            (stringOrList as List)*.toString()
        } else {
            stringOrList.toString().tokenize()
        }
    }

    // allow Command objects (hook, downloader, transcoder) to be assigned
    // via String, List<String> or Command
    public static <T extends Command> T toCommand(Class<T> klass, Object object) {
        if (object == null) {
            null
        } else if (object instanceof List) {
            def list = (object as List)
            def executable = list.remove(0)
            assert executable
            def command = klass.newInstance()
            command.executable = executable
            command.args = list
            // println "XXX: converted ${list} to ${command}"
            command
        } else if (klass.isAssignableFrom(object.class)) {
            klass.cast(object)
        } else {
            // FIXME use Apache CommandLine to tokenize correctly
            def list = object.toString().tokenize()
            assert list
            def executable = list.remove(0)
            assert executable
            def command = klass.newInstance()
            command.executable = executable
            command.args = list
            // println "XXX: converted ${object.toString()} to ${command}"
            command
        }
    }

    public static <T> T guard(T defaultValue, Closure closure) {
        T result
        try {
            result = closure() as T
        } catch (Exception e) {
            result = defaultValue
        }
        return result
    }

    public static String quoteURI(String uri) {
        // double quote a URI to make it safe for cmd.exe
        // XXX need to test this
        return Platform.isWindows() ? '"' + uri.replaceAll('"', '%22') + '"' : uri
    }

    public static boolean fileExists(String path) {
        path && fileExists(new File(path))
    }

    public static boolean fileExists(File file) {
        (file != null) && file.exists() && file.isFile()
    }

    public static boolean directoryExists(String path) {
        path && directoryExists(new File(path))
    }

    public static boolean directoryExists(File file) {
        (file != null) && file.exists() && file.isDirectory()
    }
}
