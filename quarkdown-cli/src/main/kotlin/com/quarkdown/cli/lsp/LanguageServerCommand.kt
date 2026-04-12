package com.quarkdown.cli.lsp

import com.github.ajalt.clikt.core.CliktCommand
import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.lsp.QuarkdownLanguageServerLauncher

/**
 * Command to start the Quarkdown Language Server.
 */
class LanguageServerCommand : CliktCommand("language-server") {
    override fun run() {
        // The distribution directory which contains lib/, docs/, etc.
        val quarkdownDirectory = InstallLayout.getOrNull?.file?.parentFile
        QuarkdownLanguageServerLauncher(quarkdownDirectory).startListening()
    }
}
