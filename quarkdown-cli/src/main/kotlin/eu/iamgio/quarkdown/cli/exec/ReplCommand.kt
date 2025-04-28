package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.cli.CliOptions
import eu.iamgio.quarkdown.cli.exec.strategy.ReplExecutionStrategy

/**
 * Command to start Quarkdown in interactive REPL mode.
 * @see ReplExecutionStrategy
 */
class ReplCommand : ExecuteCommand("repl") {
    override fun createExecutionStrategy(cliOptions: CliOptions) = ReplExecutionStrategy()
}
