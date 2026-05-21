package com.quarkdown.cli.doctor

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.quarkdown.cli.doctor.get.DoctorGetCommand

/**
 * Top-level command for Quarkdown installation diagnostics and introspection.
 *
 * Hosts a hierarchy of subcommands that report information about the running
 * Quarkdown distribution. Currently exposes [DoctorGetCommand] for retrieving
 * individual values (e.g. paths) suitable for shell substitution, with room for
 * diagnostic subcommands (e.g. `check`) to be added later.
 */
class DoctorCommand : CliktCommand("doctor") {
    init {
        subcommands(DoctorGetCommand())
    }

    override fun run() = Unit
}
