package eu.iamgio.quarkdown.pdf.html

/**
 * Wrapper for launching scripts via Node.js.
 * @param path path to the Node.js executable
 */
data class NodeJsWrapper(
    val path: String = DEFAULT_NODEJS_PATH,
) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
    }

    /**
     * @return whether Node.js is found from [path] and works
     */
    val isValid: Boolean
        get() =
            try {
                eval("console.log('Hello!')") == "Hello!\n"
            } catch (e: Exception) {
                false
            }

    val isPuppeteerInstalled: Boolean
        get() = eval("require('puppeteer')") == ""

    /**
     * Runs an expression or code snippet via Node.js from [path].
     * @param code the code to run
     * @return the stdout and stderr of the execution
     */
    fun eval(code: String): String {
        val process =
            ProcessBuilder(path, "-e", code)
                .redirectErrorStream(true)
                .start()
        return process.inputStream.bufferedReader().readText()
    }

    companion object {
        const val DEFAULT_NODEJS_PATH = "node"
    }
}
