package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.ReplExecutionStrategy

/**
 * Command to start Quarkdown in interactive REPL mode.
 * The result of each input is printed to standard output, and no file is written.
 * @see ReplExecutionStrategy
 */
class ReplCommand : ExecuteCommand("repl") {
    override fun finalizeCliOptions(original: CliOptions) = original.copy(outputDirectory = null, pipe = true)

    override fun createExecutionStrategy(cliOptions: CliOptions) = ReplExecutionStrategy()
}
