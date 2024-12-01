package eu.iamgio.quarkdown.cli.exec

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import eu.iamgio.quarkdown.cli.CliOptions
import eu.iamgio.quarkdown.cli.exec.strategy.FileExecutionStrategy
import java.io.File

/**
 * Command to compile a Quarkdown file into an output.
 * @see FileExecutionStrategy
 */
class CompileCommand : ExecuteCommand("c") {
    /**
     * Quarkdown source file to process.
     */
    private val source: File by argument(help = "Source file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
    )

    override fun finalizeCliOptions(original: CliOptions) = original.copy(source = source)

    override fun createExecutionStrategy(cliOptions: CliOptions) = FileExecutionStrategy(source)
}
