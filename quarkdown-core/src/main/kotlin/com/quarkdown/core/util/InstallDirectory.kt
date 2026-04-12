package com.quarkdown.core.util

import com.quarkdown.core.log.Log
import java.io.File

/**
 * The executable JAR file (or classes directory) location of the currently running process,
 * if available. Determined from the protection domain of a class loaded from this module.
 */
val thisExecutableFile: File? by lazy {
    object {}
        .javaClass.protectionDomain
        ?.codeSource
        ?.location
        ?.toURI()
        ?.let(::File)
}

/**
 * Path, relative to this module's JAR (`quarkdown-core/build/libs/quarkdown-core.jar`),
 * to the dev-time install lib directory assembled by the `assembleDevLib` Gradle task
 * (`<rootProject>/build/dev-lib`), which mirrors the distribution `lib/` layout.
 *
 * Gradle always materializes `quarkdown-core` as a JAR at this location when it is
 * consumed as a cross-module dependency (by `quarkdown-cli` via `./gradlew run`, by
 * `quarkdown-test` during tests, etc.), so this path is stable.
 */
private const val DEV_INSTALL_DIR_RELATIVE_PATH = "../../../../build/dev-lib"

/**
 * Resolves the Quarkdown install `lib/` directory, which contains bundled resources
 * such as `qd/` and `html/`.
 *
 * - Distribution (`installDist`): the JAR's parent directory (`<install>/lib/`) is the
 *   real install lib directory, identified by the presence of an `html/` subdirectory.
 * - Development (`gradle run` / IntelliJ / test runs): the dev-lib layout assembled by
 *   the `assembleDevLib` Gradle task, at a fixed relative path from this module's JAR.
 */
fun resolveInstallDirectory(): File? {
    val executable = thisExecutableFile ?: return null

    // Distribution: the JAR sits inside <install>/lib/, which already carries the `html/` layout.
    val parent = executable.parentFile
    val installDirectory =
        if (parent != null && parent.resolve("html").isDirectory) {
            parent
        } else {
            // Dev: navigate from this module's JAR to the root project's `build/dev-lib`.
            executable.resolve(DEV_INSTALL_DIR_RELATIVE_PATH).canonicalFile.takeIf { it.isDirectory }
        }

    Log.debug { "Resolved install directory: $installDirectory" }
    return installDirectory
}
