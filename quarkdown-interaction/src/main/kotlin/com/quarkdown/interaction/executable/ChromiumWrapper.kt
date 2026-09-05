package com.quarkdown.interaction.executable

import com.quarkdown.interaction.Env
import java.io.File

/**
 * Wrapper for a Chromium-family browser executable, used to print HTML documents to PDF via the Chrome DevTools Protocol.
 * @param path path to the browser executable
 */
data class ChromiumWrapper(
    override val path: String,
    override val workingDirectory: File? = null,
) : ExecutableWrapper() {
    override val isValid: Boolean
        get() =
            try {
                version().isNotBlank()
            } catch (e: Exception) {
                false
            }

    init {
        validate()
    }

    /**
     * @return the browser version, as reported by the executable via `--version`
     *         (e.g. `Chrome Headless Shell 140.0.7339.80`)
     */
    fun version(): String = launchAndGetOutput("--version").trim()

    /**
     * Starts the browser as a detached process, without waiting for it to exit.
     * stderr is merged into stdout, so DevTools startup messages can be read from [Process.getInputStream].
     * @param args arguments to pass to the executable
     * @return the running browser process
     */
    fun start(vararg args: String): Process =
        ProcessBuilder(path, *args)
            .directory(workingDirectory)
            .redirectErrorStream(true)
            .start()

    companion object : WithDefaultPath {
        /**
         * Default name of the browser executable, resolved on `PATH`.
         */
        private const val DEFAULT_NAME = "chrome-headless-shell"

        /**
         * Default path to the browser executable:
         * the `QD_CHROME_PATH` environment variable if set, or [DEFAULT_NAME] on `PATH` otherwise.
         */
        override val defaultPath: String
            get() = Env.chromePath ?: DEFAULT_NAME
    }
}
