package com.quarkdown.cli.lsp

import com.github.ajalt.clikt.core.CliktCommand
import com.quarkdown.lsp.QuarkdownLanguageServerLauncher

/**
 *
 */
class LanguageServerCommand : CliktCommand("language-server") {
    override fun run() {
        QuarkdownLanguageServerLauncher.startListening()
    }
}
