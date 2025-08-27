package com.quarkdown.cli.lsp

import com.github.ajalt.clikt.core.CliktCommand
import com.quarkdown.cli.util.thisExecutableFile
import com.quarkdown.lsp.QuarkdownLanguageServerLauncher

/**
 * Command to start the Quarkdown Language Server.
 */
class LanguageServerCommand : CliktCommand("language-server") {
    override fun run() {
        // The distribution directory which contains lib/, docs/, etc.
        val quarkdownDirectory = thisExecutableFile?.parentFile?.parentFile
        QuarkdownLanguageServerLauncher(quarkdownDirectory).startListening()
    }
}
