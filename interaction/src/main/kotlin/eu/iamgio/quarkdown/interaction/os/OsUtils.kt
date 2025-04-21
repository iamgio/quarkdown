package eu.iamgio.quarkdown.interaction.os

object OsUtils {
    /**
     * The name of the current operating system.
     */
    private val osName: String by lazy { System.getProperty("os.name").lowercase() }

    /**
     * @return whether the current operating system is Windows
     */
    private fun isWindows(): Boolean = "win" in osName

    /**
     * @return whether the current operating system is Unix-like
     */
    private fun isUnix(): Boolean = "nix" in osName || "nux" in osName || "mac" in osName

    /**
     * Runs the given [windows] or [unix] function depending on the current operating system.
     * @return the result of the function based on the OS
     */
    private fun <T> dependent(
        windows: () -> T,
        unix: () -> T,
    ) = when {
        isWindows() -> windows()
        isUnix() -> unix()
        else -> throw UnsupportedOperationException("Unexpected OS: $osName")
    }

    /**
     * @param basePath the base path of the executable
     * @return the path to the executable, with `.cmd` appended on Windows
     */
    fun cmdBasedExecutablePath(basePath: String): String =
        dependent(
            windows = { "$basePath.cmd" },
            unix = { basePath },
        )
}
