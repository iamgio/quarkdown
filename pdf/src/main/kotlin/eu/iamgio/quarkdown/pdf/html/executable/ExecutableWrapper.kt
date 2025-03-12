package eu.iamgio.quarkdown.pdf.html.executable

import java.io.File

/**
 *
 */
abstract class ExecutableWrapper {
    abstract val path: String
    abstract val isValid: Boolean
    abstract val workingDirectory: File?

    protected fun getCommandOutput(
        vararg args: String,
        workingDirectory: File? = this.workingDirectory,
    ): String {
        val process =
            ProcessBuilder(path, *args)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start()

        process.waitFor()

        fun readOutput() = process.inputStream.bufferedReader().readText()

        if (process.exitValue() != 0) {
            throw IllegalStateException("Command failed with non-zero exit code:\n${readOutput()}")
        }

        return readOutput()
    }
}
