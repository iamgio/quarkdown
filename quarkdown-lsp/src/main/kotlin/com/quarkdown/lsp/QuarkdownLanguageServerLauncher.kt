package com.quarkdown.lsp

import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.File

/**
 * Launcher for the Quarkdown Language Server.
 * @param quarkdownDirectory the directory containing the Quarkdown distribution, if available
 */
class QuarkdownLanguageServerLauncher(
    quarkdownDirectory: File?,
) {
    private val languageServer = QuarkdownLanguageServer(quarkdownDirectory)

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
        languageServer.connect(client)

        launcher.startListening()
    }
}
