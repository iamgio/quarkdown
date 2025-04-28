package eu.iamgio.quarkdown.interaction.executable

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
     * @see eval
     */
    fun eval(
        code: Reader,
        vararg argv: String,
    ): String = eval(code.readText(), *argv)

    /**
     * @return whether the given [module] is linked to the project located in [workingDirectory]
     */
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
