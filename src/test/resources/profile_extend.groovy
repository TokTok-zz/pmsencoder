script {
    profile ('Base') {
        pattern {
            domain 'inherit.pattern'
        }

        action {
            args {
                set '-base'
            }
        }
    }

    profile ('Inherit Pattern', extends: 'Base') {
        action {
            args {
                set '-inherit': 'pattern'
            }
        }
    }

    profile ('Inherit Action', extends: 'Base') {
        pattern {
            domain 'inherit.action'
        }
    }
}
