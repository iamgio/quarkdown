package eu.iamgio.quarkdown.pdf.html.executable

/**
 * Wrapper for launching scripts via Node.js.
 * @param path path to the Node.js executable
 */
data class NodeJsWrapper(
    override val path: String = DEFAULT_PATH,
) : ExecutableWrapper() {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
    }

    /**
     * @return whether Node.js is found from [path] and works
     */
    override val isValid: Boolean
        get() =
            try {
                eval("console.log('Hello!')") == "Hello!\n"
            } catch (e: Exception) {
                false
            }

    /**
     * Runs an expression or code snippet via Node.js from [path].
     * @param code the code to run
     * @return the stdout and stderr of the execution
     */
    fun eval(code: String): String = super.getCommandOutput("-e", code)

    companion object {
        const val DEFAULT_PATH = "node"
    }
}
