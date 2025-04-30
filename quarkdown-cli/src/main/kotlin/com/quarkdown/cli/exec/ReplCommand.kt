package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.ReplExecutionStrategy

/**
 * Command to start Quarkdown in interactive REPL mode.
 * @see ReplExecutionStrategy
 */
class ReplCommand : ExecuteCommand("repl") {
    override fun createExecutionStrategy(cliOptions: CliOptions) = ReplExecutionStrategy()
}
