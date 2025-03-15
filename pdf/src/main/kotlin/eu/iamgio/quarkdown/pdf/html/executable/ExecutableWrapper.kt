package eu.iamgio.quarkdown.pdf.html.executable

import eu.iamgio.quarkdown.log.Log
import java.io.File
import kotlin.streams.asSequence

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

        val output =
            process.inputStream
                .bufferedReader()
                .lines()
                .asSequence()
                // .peek(Log::debug)
                .onEach(Log::info)
                .joinToString(separator = "\n")

        process.waitFor()

        if (process.exitValue() != 0) {
            throw IllegalStateException("Command failed with non-zero exit code:\n$output")
        }

        return output
    }
}
