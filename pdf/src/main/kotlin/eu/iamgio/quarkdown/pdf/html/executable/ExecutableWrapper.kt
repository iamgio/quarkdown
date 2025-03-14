package eu.iamgio.quarkdown.pdf.html.executable

import eu.iamgio.quarkdown.log.Log
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

        val outputBuffer = StringBuilder()

        val output =
            process.inputStream
                .bufferedReader()
                .lines()
                // .peek(Log::debug)
                .peek(Log::info)
                .forEach { outputBuffer.append(it).append("\n") }

        process.waitFor()

        if (process.exitValue() != 0) {
            throw IllegalStateException("Command failed with non-zero exit code:\n$output")
        }

        return output.toString()
    }
}
