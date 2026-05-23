package com.quarkdown.interaction.executable

import java.io.File
import java.io.Reader

/**
 * Wrapper for launching scripts via Node.js.
 * @param path path to the Node.js executable
 */
data class NodeJsWrapper(
    override val path: String,
    override val workingDirectory: File,
) : ExecutableWrapper() {
    override val isValid: Boolean
        get() =
            try {
                eval("console.log('Hello!')").trim() == "Hello!"
            } catch (e: Exception) {
                false
            }

    init {
        validate()
    }

    /**
     * Runs an expression or code snippet via Node.js from [path].
     * @param code the code to run
     * @param argv arguments to pass to the script
     * @return the stdout and stderr of the execution
     */
    fun eval(
        code: String,
        vararg argv: String,
    ): String = launchAndGetOutput("-e", code, *argv)

    /**
     * @return the absolute path of the running Node.js executable, as reported by Node itself
     *         via `process.execPath`. Works regardless of how the executable was launched
     *         (PATH lookup, symlink, absolute path), avoiding platform-specific `which`/`where` calls.
     */
    fun getProcessPath(): String = eval("console.log(process.execPath)").trim()

    /**
     * @return the Node.js version, as reported by Node itself via `process.version`
     *         (e.g. `v25.6.1`).
     */
    fun getVersion(): String = eval("console.log(process.version)").trim()

    /**
     * @param module the Node.js module to look up
     * @return the absolute filesystem path of the [module]'s package root, as resolved by Node via
     *         `require.resolve('<module>/package.json')`. Reflects the same resolution rules
     *         Quarkdown uses at runtime (`NODE_PATH`, local `node_modules`, etc.).
     * @throws Exception if the module cannot be resolved (not installed, or not on the resolution path)
     */
    fun getModulePath(module: NodeModule): String =
        eval(
            "console.log(require('path').dirname(require.resolve(process.argv[1] + '/package.json')))",
            module.name,
        ).trim()

    /**
     * @param module the Node.js module to look up
     * @return the version string of the [module], read from its `package.json`.
     * @throws Exception if the module cannot be resolved
     */
    fun getModuleVersion(module: NodeModule): String =
        eval(
            "console.log(require(process.argv[1] + '/package.json').version)",
            module.name,
        ).trim()

    /**
     * @see eval
     */
    fun eval(
        code: Reader,
        vararg argv: String,
    ): String = eval(code.readText(), *argv)

    /**
     * @return whether the given [module] is linked to the project located in [workingDirectory]
     */
    @Deprecated("Not used anymore since v1.6.0")
    fun isLinked(module: NodeModule): Boolean =
        try {
            eval("require('${module.name}')").isEmpty()
        } catch (e: Exception) {
            false
        }

    companion object : WithDefaultPath {
        /**
         * Default path to the Node.js executable.
         */
        private const val DEFAULT_PATH = "node"

        override val defaultPath: String
            get() = DEFAULT_PATH
    }
}
