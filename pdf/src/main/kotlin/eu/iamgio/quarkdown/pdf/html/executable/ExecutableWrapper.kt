package eu.iamgio.quarkdown.pdf.html.executable

import java.io.File

/**
 *
 */
abstract class ExecutableWrapper {
    abstract val path: String
    abstract val isValid: Boolean

    protected fun getCommandOutput(
        vararg args: String,
        workingDirectory: File? = null,
    ): String {
        val process =
            ProcessBuilder(path, *args)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start()

        return process.inputStream.bufferedReader().readText()
    }
}
