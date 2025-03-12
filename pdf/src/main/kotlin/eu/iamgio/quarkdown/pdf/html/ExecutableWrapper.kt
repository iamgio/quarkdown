package eu.iamgio.quarkdown.pdf.html

/**
 *
 */
abstract class ExecutableWrapper {
    abstract val path: String
    abstract val isValid: Boolean

    protected fun getCommandOutput(vararg args: String): String {
        val process =
            ProcessBuilder(path, *args)
                .redirectErrorStream(true)
                .start()
        return process.inputStream.bufferedReader().readText()
    }
}
