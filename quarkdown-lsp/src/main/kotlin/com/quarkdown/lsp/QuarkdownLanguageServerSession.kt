package com.quarkdown.lsp

import org.eclipse.lsp4j.jsonrpc.MessageConsumer
import org.eclipse.lsp4j.jsonrpc.MessageProducer
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.logging.Level
import java.util.logging.Logger

private val logger = Logger.getLogger(QuarkdownLanguageServerSession::class.java.name)

/**
 * Hosts a single Quarkdown Language Server session driven by message callbacks rather than streams.
 *
 * This is the message-typed counterpart of [QuarkdownLanguageServerLauncher], which speaks the
 * `Content-Length`-framed byte stream form expected by stdio. Use this one for transports that
 * already deliver one JSON-RPC payload per message (WebSockets, in-process channels, ...): the
 * `Launcher` is wired manually so there is no need to bridge through pipes and reapply framing.
 *
 * @param quarkdownDirectory the directory containing the Quarkdown distribution, if available
 * @param executor executor for LSP4J dispatch, background work, and the listener loop
 * @param onExit hook invoked when the client sends the LSP `exit` notification
 */
class QuarkdownLanguageServerSession(
    private val quarkdownDirectory: File?,
    private val executor: ExecutorService,
    private val onExit: () -> Unit = {},
) {
    /**
     * Runs the session synchronously, returning when the LSP4J listener exits.
     *
     * The listener exits when [pollMessage] returns `null` (signaling that the client has
     * disconnected). While running, the server invokes [send] to deliver server-to-client
     * messages as raw JSON-RPC payloads.
     *
     * @param pollMessage blocks until the next client message; returns `null` to signal close
     * @param send called by the server to push a JSON-RPC message to the client
     */
    fun run(
        pollMessage: () -> String?,
        send: (String) -> Unit,
    ) {
        val server = QuarkdownLanguageServer(quarkdownDirectory, executor, onExit)
        val jsonHandler = MessageJsonHandler(supportedMethods())

        val outbound = MessageConsumer { msg -> send(jsonHandler.serialize(msg)) }
        val remoteEndpoint = RemoteEndpoint(outbound, ServiceEndpoints.toEndpoint(server))
        remoteEndpoint.jsonHandler = jsonHandler

        server.connect(ServiceEndpoints.toServiceObject(remoteEndpoint, LanguageClient::class.java))

        val producer = CallbackMessageProducer(jsonHandler, pollMessage)
        ConcurrentMessageProcessor(producer, remoteEndpoint)
            .beginProcessing(executor)
            .get()
    }

    private fun supportedMethods(): Map<String, JsonRpcMethod> =
        LinkedHashMap<String, JsonRpcMethod>().apply {
            putAll(ServiceEndpoints.getSupportedMethods(LanguageClient::class.java))
            putAll(ServiceEndpoints.getSupportedMethods(LanguageServer::class.java))
        }
}

/**
 * [MessageProducer] that pulls raw JSON-RPC payloads from a polling callback instead of a stream.
 * Returns from [listen] when the callback returns `null`, which is the transport's signal that
 * the connection is closed.
 */
private class CallbackMessageProducer(
    private val jsonHandler: MessageJsonHandler,
    private val poll: () -> String?,
) : MessageProducer {
    override fun listen(messageConsumer: MessageConsumer) {
        while (true) {
            val payload = poll() ?: return
            try {
                messageConsumer.consume(jsonHandler.parseMessage(payload))
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Failed to dispatch incoming message", e)
            }
        }
    }
}
