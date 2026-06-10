package com.quarkdown.lsp

import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.exitProcess

/**
 * Launcher for the Quarkdown Language Server.
 *
 * Each launcher instance wraps a single client connection. Hosts that need to serve multiple
 * clients in the same JVM (e.g. WebSockets) should construct one launcher
 * per connection, passing the per-connection streams and a JVM-wide shared [executor].
 *
 * @param quarkdownDirectory the directory containing the Quarkdown distribution, if available
 * @param input the input stream to read JSON-RPC messages from. Defaults to [System.in].
 * @param output the output stream to write JSON-RPC messages to. Defaults to [System.out].
 * @param executor the executor service to dispatch JSON-RPC requests and background work on.
 *                  Defaults to a per-launcher cached thread pool. Pass a shared instance when
 *                  hosting multiple servers in one JVM.
 * @param onExit hook invoked when the client sends the LSP `exit` notification. Defaults to
 *                terminating the JVM, which is appropriate for stdio mode.
 */
class QuarkdownLanguageServerLauncher(
    quarkdownDirectory: File?,
    private val input: InputStream = System.`in`,
    private val output: OutputStream = System.out,
    private val executor: ExecutorService = Executors.newCachedThreadPool(),
    onExit: () -> Unit = { exitProcess(0) },
) {
    private val languageServer = QuarkdownLanguageServer(quarkdownDirectory, executor, onExit)

    private val launcher by lazy {
        Launcher
            .Builder<LanguageClient>()
            .setLocalService(languageServer)
            .setRemoteInterface(LanguageClient::class.java)
            .setInput(input)
            .setOutput(output)
            .setExecutorService(executor)
            .create()
            .let(::requireNotNull)
    }

    /**
     * Connects the server to the client and starts listening for incoming JSON-RPC messages.
     * @return the future that completes when the client disconnects
     */
    fun startListening(): Future<Void> {
        val client: LanguageClient = requireNotNull(launcher.remoteProxy)
        languageServer.connect(client)
        return launcher.startListening()
    }
}
