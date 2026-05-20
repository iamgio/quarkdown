package com.quarkdown.cli.doctor.get

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.quarkdown.installlayout.InstallLayout

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
 *
 * Exits with a non-zero status if the install directory cannot be resolved (for example, when
 * Quarkdown is run outside its standard distribution layout and the dev-lib layout is unavailable).
 */
class DoctorGetInstallDirCommand : CliktCommand("install-dir") {
    override fun run() {
        val installDirectory =
            InstallLayout.getOrNull?.file?.parentFile
                ?: throw CliktError(
                    "Cannot resolve the Quarkdown install directory. " +
                        "This usually means Quarkdown is being run outside its standard distribution layout.",
                )

        echo(installDirectory.absolutePath)
    }
}
