package com.quarkdown.cli.doctor.get

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/**
 * Command group that retrieves individual pieces of information about the Quarkdown installation.
 *
 * Each leaf subcommand prints a single value to standard output, intended for scripting and shell substitution.
 */
class DoctorGetCommand : CliktCommand("get") {
    init {
        subcommands(DoctorGetInstallDirCommand())
    }

    override fun run() = Unit
}
