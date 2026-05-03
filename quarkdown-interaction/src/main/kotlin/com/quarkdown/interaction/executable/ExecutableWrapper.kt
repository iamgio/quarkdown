package com.quarkdown.interaction.executable

import com.quarkdown.core.log.Log
import java.io.File
import kotlin.streams.asSequence

/**
 * Wrapper for a third-party executable program.
 */
abstract class ExecutableWrapper {
    /**
     * Path to the executable program.
     */
    abstract val path: String

    /**
     * @return whether the executable is found and works as expected
     */
    abstract val isValid: Boolean

    /**
     * Working directory to run the executable in.
     * If `null`, the current working directory will be used.
     */
    abstract val workingDirectory: File?

    /**
     * @throws IllegalStateException if the path is blank
     */
    protected fun validate() {
        require(path.isNotBlank()) { "Path cannot be blank" }
    }

    /**
     * @param args arguments to pass to the executable
     * @param workingDirectory working directory to run the executable in. Defaults to [workingDirectory]
     * @return the process builder
     */
    private fun createProcessBuilder(
        vararg args: String,
        workingDirectory: File? = this.workingDirectory,
    ): ProcessBuilder =
        ProcessBuilder(path, *args)
            .directory(workingDirectory)
            .redirectErrorStream(true)

    /**
     * @param args arguments to pass to the executable
     * @param workingDirectory working directory to run the executable in
     * @return the stdout and stderr of the execution
     * @throws InterruptedException if the current thread is interrupted while waiting for the process
     */
    protected fun launchAndGetOutput(
        vararg args: String,
        workingDirectory: File? = this.workingDirectory,
    ): String {
        val process = createProcessBuilder(*args, workingDirectory = workingDirectory).start()

        try {
            val output =
                process.inputStream
                    .bufferedReader()
                    .lines()
                    .asSequence()
                    .onEach(Log::debug)
                    .onEach { line ->
                        if (Thread.currentThread().isInterrupted) {
                            throw InterruptedException()
                        }
                    }.joinToString(separator = "\n")

            process.waitFor()

            Log.debug("Command `$path ${args.joinToString()}` exited with code ${process.exitValue()}. Output:\n$output")

            if (process.exitValue() != 0) {
                throw IllegalStateException("Command failed with non-zero exit code:\n$output")
            }

            return output
        } catch (e: InterruptedException) {
            process.destroyForcibly()
            Thread.currentThread().interrupt()
            throw e
        }
    }
}
