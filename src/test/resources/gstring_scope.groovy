script {
    def ncores = pms.getConfiguration().getNumberOfCpuCores()
    def var1 = "config$ncores"

    profile ('GString Scope') {
        def var2 = "profile$ncores"
        def var3

        pattern {
            var3 = "pattern$ncores"
            match { 1 == 1 }
        }

        action {
            def var4 = "action$ncores"
            transcoder = [ 'transcoder', var1, var2, var3, var4 ]
        }
    }
}
