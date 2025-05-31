package com.quarkdown.lsp

import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient

/**
 *
 */
object QuarkdownLanguageServerLauncher {
    private val languageServer = QuarkdownLanguageServer()

    private val launcher by lazy {
        Launcher
            .Builder<LanguageClient>()
            .setLocalService(languageServer)
            .setRemoteInterface(LanguageClient::class.java)
            .setInput(System.`in`)
            .setOutput(System.out)
            .create()
            .let(::requireNotNull)
    }

    fun startListening() {
        val client: LanguageClient = requireNotNull(launcher.remoteProxy)

        launcher.startListening()
    }
}
