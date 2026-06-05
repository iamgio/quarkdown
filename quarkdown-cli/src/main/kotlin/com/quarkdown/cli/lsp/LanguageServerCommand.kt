package com.quarkdown.cli.lsp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.lsp.QuarkdownLanguageServerLauncher
import com.quarkdown.lsp.QuarkdownLanguageServerSession
import com.quarkdown.server.jsonrpc.JsonRpcWebSocketServer
import com.quarkdown.server.jsonrpc.TextMessageHandler
import java.util.concurrent.Executors

/**
 * The default port to start the WebSocket language server on.
 */
const val DEFAULT_LANGUAGE_SERVER_WS_PORT = 8090

/**
 * The default WebSocket endpoint path for the language server.
 */
const val DEFAULT_LANGUAGE_SERVER_WS_PATH = "/lsp"

/**
 * Command to start the Quarkdown Language Server over standard input/output.
 *
 * This is the conventional transport used by editor extensions that launch the language server
 * as a child process. For multi-client deployments (e.g. browser-based editors), use
 * [LanguageServerWebSocketCommand] instead.
 */
class LanguageServerCommand : CliktCommand("language-server") {
    override fun run() {
        val quarkdownDirectory = InstallLayout.getOrNull?.file?.parentFile
        QuarkdownLanguageServerLauncher(quarkdownDirectory).startListening()
    }
}

/**
 * Command to start the Quarkdown Language Server over WebSockets.
 *
 * Each WebSocket connection gets its own [QuarkdownLanguageServerSession], all sharing one JVM
 * and a single thread pool. This is the transport used by browser-based editors such as Monaco
 * via `monaco-languageclient`, which sends one JSON-RPC message per WebSocket text frame.
 */
class LanguageServerWebSocketCommand : CliktCommand("language-server-ws") {
    /**
     * Port to start the WebSocket server on.
     */
    private val port: Int by option("-p", "--port", help = "Port to start the WebSocket server on")
        .int()
        .default(DEFAULT_LANGUAGE_SERVER_WS_PORT)

    /**
     * WebSocket endpoint path that clients connect to.
     */
    private val path: String by option("--path", help = "WebSocket endpoint path that clients connect to")
        .default(DEFAULT_LANGUAGE_SERVER_WS_PATH)

    override fun run() {
        val quarkdownDirectory = InstallLayout.getOrNull?.file?.parentFile

        // One pool for the whole JVM, shared across all per-connection language servers.
        val executor = Executors.newCachedThreadPool()

        val handler =
            TextMessageHandler { pollMessage, send ->
                QuarkdownLanguageServerSession(
                    quarkdownDirectory = quarkdownDirectory,
                    executor = executor,
                    onExit = {},
                ).run(pollMessage, send)
            }

        JsonRpcWebSocketServer(path, handler).start(port, wait = true)
    }
}
