package com.quarkdown.cli.doctor.get

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.quarkdown.installlayout.InstallLayout
import java.io.File

/**
 * Command group that retrieves individual pieces of information about the Quarkdown installation.
 *
 * Each leaf subcommand prints a single value to standard output, intended for scripting and shell substitution.
 */
class DoctorGetCommand : CliktCommand("get") {
    init {
        subcommands(
            InstallDirCommand(),
            AgentSkillCommand(),
        )
    }

    override fun run() = Unit

    /**
     * Prints the absolute path of the Quarkdown install directory to standard output.
     *
     * In a Quarkdown distribution, this is the root directory containing `bin/`, `lib/`, and `docs/`.
     * Intended for shell substitution, replacing fragile cross-platform symlink-resolution snippets:
     *
     * ```bash
     * INSTALL="$(quarkdown doctor get install-dir)"
     * ls "$INSTALL/docs"
     * ```
     */
    class InstallDirCommand :
        AbstractDoctorGetPathCommand(
            name = "install-dir",
            description = "Quarkdown install directory",
        ) {
        override fun getFile(installLayout: InstallLayout): File? = installLayout.file.parentFile
    }

    /**
     * Prints the absolute path of the bundled agent skill directory to standard output.
     *
     * The directory contains the `SKILL.md` entrypoint and any supporting files that an AI agent tool
     * (e.g. Claude Code) can install or read as context. Typical use is to symlink it into the user's
     * skills directory:
     *
     * ```bash
     * ln -s "$(quarkdown doctor get agent-skill)" ~/.claude/skills/quarkdown
     * ```
     */
    class AgentSkillCommand :
        AbstractDoctorGetPathCommand(
            name = "agent-skill",
            description = "bundled agent skill directory",
        ) {
        override fun getFile(installLayout: InstallLayout): File = installLayout.agentSkill.file
    }
}
