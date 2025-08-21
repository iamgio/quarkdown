package com.quarkdown.lsp

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.completion.CompletionSuppliersFactory
import com.quarkdown.lsp.highlight.SemanticTokensSuppliersFactory
import com.quarkdown.lsp.highlight.TokenType
import com.quarkdown.lsp.hover.HoverSuppliersFactory
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * Quarkdown Language Server implementation.
 * @param quarkdownDirectory the directory containing the Quarkdown distribution, if available
 */
class QuarkdownLanguageServer(
    private val quarkdownDirectory: File?,
) : LanguageServer,
    LanguageClientAware {
    private val textDocumentService: TextDocumentService =
        QuarkdownTextDocumentService(
            this,
            CompletionSuppliersFactory(this).default(),
            SemanticTokensSuppliersFactory().default(),
            HoverSuppliersFactory(this).default(),
        )

    private val completionTriggers =
        with(QuarkdownPatterns.FunctionCall) {
            listOf(
                BEGIN,
                CHAIN_SEPARATOR.last().toString(),
                ARGUMENT_BEGIN,
            )
        }

    private val workspaceService: WorkspaceService = QuarkdownWorkspaceService(this)

    private lateinit var client: LanguageClient

    /**
     * The directory containing the documentation files, if available.
     * This is located in the Quarkdown distribution.
     */
    val docsDirectory: File?
        get() = quarkdownDirectory?.resolve("docs")?.takeIf { it.isDirectory }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
        val legend =
            SemanticTokensLegend(
                TokenType.legend,
                emptyList(),
            )

        val serverCaps =
            ServerCapabilities().apply {
                textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
                completionProvider = CompletionOptions(true, completionTriggers)
                hoverProvider = Either.forLeft(true)
                semanticTokensProvider = SemanticTokensWithRegistrationOptions(legend, true, null)
            }
        val response = InitializeResult(serverCaps)

        // Caching the available function catalogue for improved performance.
        thread {
            docsDirectory?.let(CacheableFunctionCatalogue::storeCatalogue)
        }

        return CompletableFuture.completedFuture(response)
    }

    override fun shutdown(): CompletableFuture<in Any>? = CompletableFuture.completedFuture(null)

    override fun exit() = exitProcess(0)

    override fun getTextDocumentService() = textDocumentService

    override fun getWorkspaceService() = workspaceService

    override fun connect(client: LanguageClient?) {
        this.client = client ?: throw IllegalStateException("Language client cannot be null")
    }

    fun log(message: String) {
        client.logMessage(MessageParams(MessageType.Log, message))
    }
}
