package com.quarkdown.installlayout

import com.quarkdown.core.log.Log
import java.io.File

/**
 * Resolves the Quarkdown install `lib/` directory and wraps it as an [InstallLayout].
 *
 * Strategies are tried in order, and the first one that yields a directory wins:
 *
 * 1. **Explicit**: the `quarkdown.home` system property or the `QUARKDOWN_HOME` environment
 *    variable. Set by a packager, or by anyone running Quarkdown in a form where the code
 *    location says nothing useful about where the installation lives.
 * 2. **Code source**: where this module's code was loaded from, which covers both a
 *    distribution (`installDist`) and a development run.
 * 3. **Running executable**: the directory of the running process's binary, assuming the
 *    distribution's `<install>/bin/<executable>` layout. This is the case when there is no JAR
 *    to inspect at all, as in a GraalVM native image.
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

    /** System property naming the install directory, taking precedence over [HOME_ENV_VARIABLE]. */
    private const val HOME_SYSTEM_PROPERTY = "quarkdown.home"

    /** Environment variable naming the install directory. */
    private const val HOME_ENV_VARIABLE = "QUARKDOWN_HOME"

    /**
     * Subdirectories that identify a directory as a Quarkdown install layout.
     */
    private val LAYOUT_MARKERS = listOf("html", "qd")

    fun resolve(): InstallLayout {
        val directory =
            fromExplicitHome()
                ?: fromCodeSource()
                ?: fromRunningExecutable()
                ?: error(unresolvedMessage())

        Log.debug { "Resolved install directory: $directory" }
        return InstallLayout(InstallLayoutDirectory(directory))
    }

    /**
     * Reads the install directory from the explicit property or environment variable.
     *
     * The value may name either the installation root (the directory containing `lib/`) or the
     * `lib/` directory itself. A value that names neither is an error rather than a fall-through:
     * having asked for a specific installation, the caller should hear that it was not usable.
     *
     * @return the install `lib/` directory, or `null` when neither is set
     */
    private fun fromExplicitHome(): File? {
        val source =
            System.getProperty(HOME_SYSTEM_PROPERTY)?.let { HOME_SYSTEM_PROPERTY to it }
                ?: System.getenv(HOME_ENV_VARIABLE)?.let { HOME_ENV_VARIABLE to it }
                ?: return null
        val (name, path) = source

        val root = File(path)
        return root.resolve(INSTALL_LIB_DIR_NAME).takeIfInstallLayout()
            ?: root.takeIfInstallLayout()
            ?: error("$name points to '$path', which is not a Quarkdown installation ")
    }

    /**
     * Resolves from the location this module's code was loaded from.
     *
     * @return the install `lib/` directory, or `null` when the code location is unavailable
     */
    private fun fromCodeSource(): File? {
        val executable = thisExecutableFile ?: return null

        // Distribution: the JAR sits inside <install>/lib/.
        val parent = executable.parentFile
        if (parent?.name == INSTALL_LIB_DIR_NAME) {
            parent.takeIfInstallLayout()?.let { return it }
        }

        // Dev: navigate from this module's JAR to the root project's `build/dev-lib`.
        return executable.resolve(DEV_INSTALL_DIR_RELATIVE_PATH).canonicalFile.takeIfInstallLayout()
    }

    /**
     * Resolves from the running process's own binary, assuming a distribution's
     * `<install>/bin/<executable>` layout.
     *
     * The result is validated, because this strategy runs for any process, including a plain
     * `java` launch whose sibling `lib/` belongs to the JDK.
     *
     * @return the install `lib/` directory, or `null` when the binary's location is unavailable
     *         or is not part of an installation
     */
    private fun fromRunningExecutable(): File? =
        ProcessHandle
            .current()
            .info()
            .command()
            .orElse(null)
            ?.let(::File)
            ?.parentFile
            ?.parentFile
            ?.resolve(INSTALL_LIB_DIR_NAME)
            ?.takeIfInstallLayout()

    /**
     * @return this directory if it holds a Quarkdown install layout, `null` otherwise
     */
    private fun File.takeIfInstallLayout(): File? =
        this.takeIf { it.isDirectory && LAYOUT_MARKERS.any { marker -> it.resolve(marker).isDirectory } }

    private fun unresolvedMessage(): String =
        """
        Cannot resolve the Quarkdown install directory.
        Code source: ${thisExecutableFile?.absolutePath ?: "unavailable"}
        Running executable: ${ProcessHandle.current().info().command().orElse("unavailable")}
        Set $HOME_ENV_VARIABLE or -D$HOME_SYSTEM_PROPERTY to the Quarkdown installation directory.
        """.trimIndent()
}
