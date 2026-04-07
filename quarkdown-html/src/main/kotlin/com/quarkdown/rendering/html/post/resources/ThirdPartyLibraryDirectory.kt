package com.quarkdown.rendering.html.post.resources

import java.io.File

/**
 * Resolves the filesystem directory containing third-party HTML library files
 * (e.g. KaTeX, Mermaid, fonts).
 *
 * At build time, `processResources` writes the absolute path of the third-party directory
 * into a classpath resource (`render/thirdparty.path`). This allows the directory to be located
 * at runtime regardless of the launch method (IDE, `./gradlew run`, or `installDist`).
 */
object ThirdPartyLibraryDirectory {
    /**
     * The directory containing third-party library files, or `null` if it cannot be resolved.
     */
    val path: File? by lazy {
        ThirdPartyLibraryDirectory::class.java
            .getResource("/render/thirdparty.path")
            ?.readText()
            ?.trim()
            ?.let(::File)
            ?.takeIf { it.isDirectory }
    }
}
