package com.quarkdown.cli.doctor.get

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.installlayout.InstallLayout

/**
 * Base class for `doctor get <name>` subcommands that print the absolute filesystem path of a
 * single entry in the Quarkdown install layout to standard output.
 *
 * Subclasses select the entry to print by overriding [getEntry], and provide a human-readable
 * [description] used in error messages. The command exits with a non-zero status if the install
 * layout cannot be resolved or if the entry does not exist on disk.
 *
 * @param name name of the leaf subcommand (e.g. `install-dir`)
 * @param description human-readable description of the entry, used in error messages
 *                    (e.g. `"Quarkdown install directory"`)
 */
abstract class AbstractDoctorGetPathCommand(
    name: String,
    private val description: String,
) : CliktCommand(name) {
    /**
     * @param installLayout the Quarkdown install layout
     * @return the file or directory whose absolute path should be printed, or `null` if it cannot be resolved.
     */
    protected abstract fun getEntry(installLayout: InstallLayout): FsEntry?

    final override fun run() {
        val entry =
            InstallLayout.getOrNull
                ?.let(::getEntry)
                ?.takeIf { it.exists }
                ?: throw CliktError(
                    "Cannot resolve the $description. " +
                        "This usually means Quarkdown is being run outside its standard distribution layout.",
                )

        echo(entry.fullPath)
    }
}
