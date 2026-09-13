package com.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.enum
import com.quarkdown.cli.creator.command.CreateProjectCommand
import com.quarkdown.cli.doctor.DoctorCommand
import com.quarkdown.cli.exec.CompileCommand
import com.quarkdown.cli.exec.ReplCommand
import com.quarkdown.cli.lsp.LanguageServerCommand
import com.quarkdown.cli.server.StartWebServerCommand
import com.quarkdown.core.log.Log
import com.quarkdown.core.log.LogLevel

/**
 * Environment variable that sets the log level (e.g. `QD_LOG_LEVEL=debug`),
 * read as a fallback when the `--log-level` option is not passed.
 */
const val LOG_LEVEL_ENV = "QD_LOG_LEVEL"

/**
 * Main command of Quarkdown CLI, which delegates to subcommands.
 */
class QuarkdownCommand : CliktCommand() {
    init {
        val version = this::class.java.getResource("/version.txt")?.readText() ?: "unknown"
        versionOption(version)
    }

    /**
     * Minimum level a message must have in order to be logged,
     * placed before the subcommand (e.g. `quarkdown --log-level debug c file.qd`).
     */
    private val logLevel: LogLevel? by option(
        "--log-level",
        envvar = LOG_LEVEL_ENV,
        help = "Minimum log level",
    ).enum<LogLevel> { it.name.lowercase() }

    override fun aliases() = mapOf("c" to listOf(CompileCommand().commandName))

    override fun run() {
        logLevel?.let { Log.level = it }
    }
}

fun main(args: Array<String>) =
    QuarkdownCommand()
        .subcommands(
            CompileCommand(),
            ReplCommand(),
            StartWebServerCommand(),
            CreateProjectCommand(),
            LanguageServerCommand(),
            DoctorCommand(),
        ).main(args)
