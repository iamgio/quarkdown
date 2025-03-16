package eu.iamgio.quarkdown.pdf.html.executable

import java.io.File

/**
 * Wrapper for launching scripts via Node.js.
 * @param path path to the Node.js executable
 */
data class NodeJsWrapper(
    override val path: String = DEFAULT_PATH,
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
     * @return the stdout and stderr of the execution
     */
    fun eval(code: String): String = launchAndGetOutput("-e", code)

    /**
     * Runs a script file via Node.js from [path].
     * @param script the script file to run
     * @param argv arguments to pass to the script
     * @return the stdout and stderr of the execution
     */
    fun evalFile(
        script: File,
        vararg argv: String,
    ): String = launchAndGetOutput(script.path, *argv)

    /**
     * @return whether the given [module] is linked to the project located in [workingDirectory]
     */
    fun isLinked(module: NodeModule): Boolean =
        try {
            eval("require('${module.name}')").isEmpty()
        } catch (e: Exception) {
            false
        }

    companion object {
        /**
         * Default path to the Node.js executable.
         */
        const val DEFAULT_PATH = "node"
    }
}
