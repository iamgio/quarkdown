package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import eu.iamgio.quarkdown.cli.exec.CompileCommand
import eu.iamgio.quarkdown.cli.exec.ReplCommand

/**
 * Main command of Quarkdown CLI, which delegates to subcommands.
 */
class QuarkdownCommand : CliktCommand() {
    override fun run() {}
}

fun main(args: Array<String>) =
    QuarkdownCommand().subcommands(
        CompileCommand(),
        ReplCommand(),
        StartWebServerCommand(),
    ).main(args)
