package com.quarkdown.installlayout

import com.quarkdown.core.log.Log
import java.io.File

/**
 * Resolves the Quarkdown install `lib/` directory and wraps it as an [InstallLayout].
 *
 * Two environments are supported:
 * - **Distribution** (`installDist`): the module's JAR sits inside `<install>/lib/`,
 *   detected by the parent directory being named `lib`.
 * - **Development** (`./gradlew run`, test runs): the module's JAR lives at
 *   `<module>/build/libs/<module>.jar`. A fixed relative path leads from there to
 *   `<rootProject>/build/dev-lib`, a mirrored layout produced by the `assembleDevLib`
 *   Gradle task.
 */
internal object InstallDirectoryResolver {
    /** Name of the install `lib/` directory in a Quarkdown distribution. */
    private const val INSTALL_LIB_DIR_NAME = "lib"

    /**
     * Path from this module's JAR (`<module>/build/libs/<module>.jar`) to the root
     * project's dev-lib layout (`<rootProject>/build/dev-lib`). Stable because Gradle
     * always materializes cross-module project dependencies as JARs at this location.
     */
    private const val DEV_INSTALL_DIR_RELATIVE_PATH = "../../../../build/dev-lib"

    fun resolve(): InstallLayout {
        val executable =
            thisExecutableFile
                ?: error("Cannot determine the Quarkdown executable location. The JAR's code source may be unavailable.")

        val directory = resolveFrom(executable)

        Log.debug { "Resolved install directory: $directory" }
        return InstallLayout(InstallLayoutDirectory(directory))
    }

    private fun resolveFrom(executable: File): File {
        // Distribution: the JAR sits inside <install>/lib/.
        val parent = executable.parentFile
        if (parent?.name == INSTALL_LIB_DIR_NAME) {
            return parent
        }

        // Dev: navigate from this module's JAR to the root project's `build/dev-lib`.
        val devLib = executable.resolve(DEV_INSTALL_DIR_RELATIVE_PATH).canonicalFile
        if (devLib.isDirectory) {
            return devLib
        }

        error(
            """
            Cannot resolve the Quarkdown install directory.
            Executable: $executable
            Tried distribution (parent named '$INSTALL_LIB_DIR_NAME'): ${parent?.absolutePath}
            Tried dev-lib: ${devLib.absolutePath}
            """.trimIndent(),
        )
    }
}
