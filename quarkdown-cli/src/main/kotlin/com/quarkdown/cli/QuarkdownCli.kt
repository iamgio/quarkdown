package com.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.quarkdown.cli.creator.command.CreateProjectCommand
import com.quarkdown.cli.exec.CompileCommand
import com.quarkdown.cli.exec.ReplCommand
import com.quarkdown.cli.lsp.LanguageServerCommand
import com.quarkdown.cli.server.StartWebServerCommand

/**
 * Main command of Quarkdown CLI, which delegates to subcommands.
 */
class QuarkdownCommand : CliktCommand() {
    override fun aliases() = mapOf("c" to listOf(CompileCommand().commandName))

    override fun run() {}
}

fun main(args: Array<String>) =
    QuarkdownCommand()
        .subcommands(
            CompileCommand(),
            ReplCommand(),
            StartWebServerCommand(),
            CreateProjectCommand(),
            LanguageServerCommand(),
        ).main(args)
