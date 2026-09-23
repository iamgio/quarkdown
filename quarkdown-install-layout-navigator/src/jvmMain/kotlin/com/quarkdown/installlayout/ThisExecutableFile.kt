package com.quarkdown.installlayout

import java.io.File

/**
 * The JAR file (or exploded classes directory) from which this module's code was loaded,
 * as reported by the class protection domain. Used as the starting point for
 * [InstallDirectoryResolver] to locate the install `lib/` directory.
 *
 * Since this property is defined in `quarkdown-install-layout-navigator`, its location is
 * determined by Gradle's dependency resolution: consumers see it as the module's JAR
 * (e.g. `quarkdown-install-layout-navigator/build/libs/...`) in dev, or as one of the JARs
 * inside `<install>/lib/` in a distribution.
 */
val thisExecutableFile: File? by lazy {
    object {}
        .javaClass.protectionDomain
        ?.codeSource
        ?.location
        ?.toURI()
        ?.let(::File)
}
