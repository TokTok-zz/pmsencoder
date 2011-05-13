@Typed

package com.chocolatey.pmsencoder

import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.tools.shell.Global

public class Browser {
    private final String ENV_CONF = 'env.conf.js'
    private final String ENVJS = 'env.rhino.js'
    private final String JQUERY = 'jquery.min.js'
    private ScriptableObject sharedScope
    private String currentLocation
    private String location

    public Browser() {
        def cx = ContextFactory.getGlobal().enterContext()

        try {
            cx.setOptimizationLevel(-1)
            cx.setLanguageVersion(Context.VERSION_1_7)

            def global = new Global()
            global.init(cx)

            // https://developer.mozilla.org/En/Rhino_documentation/Scopes_and_Contexts
            sharedScope = cx.initStandardObjects(global)

            def envReader = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("/js/${ENVJS}")
                )
            )

            cx.evaluateReader(sharedScope, envReader, ENVJS, 1, null)

            def envconfReader = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("/js/${ENV_CONF}")
                )
            )

            cx.evaluateReader(sharedScope, envconfReader, ENV_CONF, 1, null)

            def jqueryReader = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("/js/${JQUERY}")
                )
            )

            cx.evaluateReader(sharedScope, jqueryReader, JQUERY, 1, null)
        } finally {
            Context.exit()
        }
    }

    public void navigate(String uri) {
        if (uri && uri != currentLocation) {
            location = uri
        }
    }

    public String eval(String js) {
        eval(String.class, js)
    }

    public <T> T eval(Class<T> klass, String js) {
        def cx = ContextFactory.getGlobal().enterContext()

        try {
            def scope = cx.newObject(sharedScope)
            scope.setPrototype(sharedScope)
            scope.setParentScope(null)
            // XXX: there's an unapplied patch for this (in Env.js) on GitHub
            if (location) {
                cx.evaluateString(scope, "window.location = '${location}'", "<eval>", 1, null)
                currentLocation = location
                location = null
            }
            return Context.jsToJava(cx.evaluateString(scope, js, "<eval>", 1, null), klass)
        } finally {
            Context.exit()
        }
    }
}
