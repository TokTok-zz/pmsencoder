script {
    profile('Remove Name') {
        pattern {
            domain 'remove.name'
        }

        action {
            // -foo -bar -baz -quux -> -foo -baz -quux
            args {
                remove '-bar'
            }
        }
    }

    profile('Remove Value') {
        pattern {
            domain 'remove.value'
        }

        action {
            // -foo -bar baz -quux -> -foo -quux
            args {
                remove '-bar'
            }
        }
    }

    profile('Digit Value') {
        pattern {
            domain 'digit.value'
        }

        action {
            // -foo -bar -42 -quux -> -foo -quux
            args {
                remove '-bar'
            }
        }
    }

    profile('Hyphen Value') {
        pattern {
            domain 'hyphen.value'
        }

        action {
            // -foo -output - -quux -> -foo -quux
            args {
                remove '-output'
            }
        }
    }
}
