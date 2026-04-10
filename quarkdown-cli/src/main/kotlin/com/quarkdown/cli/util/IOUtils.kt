package com.quarkdown.cli.util

import com.quarkdown.core.log.Log
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * The executable JAR file location, if available.
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
 * Path, relative to the exploded classes root (`<module>/build/classes/kotlin/main`),
 * to the dev-time install lib directory assembled by Gradle (`<rootProject>/build/dev-lib`),
 * which is a mirrored layout of the distribution `lib/` directory.
 */
private const val DEV_INSTALL_DIR_RELATIVE_PATH = "../../../../../build/dev-lib"

/**
 * Resolves the Quarkdown install `lib/` directory, which contains bundled resources
 * such as `qd/` and `html/`.
 *
 * - Distribution (`installDist` JAR): the JAR's parent directory.
 * - Development (`gradle run` / IntelliJ): a location relative to the exploded classes root, assembled by the `assembleDevLib` Gradle task.
 */
fun resolveInstallDirectory(): File? {
    val executable = thisExecutableFile ?: return null

    // Distribution: JAR file, install dir is its parent.
    // Development: exploded classes, navigate relative to the classes root.
    val installDirectory =
        when {
            executable.isFile -> executable.parentFile
            else -> executable.resolve(DEV_INSTALL_DIR_RELATIVE_PATH).canonicalFile.takeIf { it.isDirectory }
        }

    Log.debug { "Resolved install directory: $installDirectory" }
    return installDirectory
}

/**
 * Cleans [this] directory by deleting all files and directories inside it.
 * Does nothing if the directory is empty or if the file does not exist or is not a directory.
 */
@OptIn(ExperimentalPathApi::class)
fun File.cleanDirectory() {
    listFiles()?.forEach { it.toPath().deleteRecursively() }
}
