package com.quarkdown.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

/**
 *
 */
class QuarkdownLanguageServer :
    LanguageServer,
    LanguageClientAware {
    private val textDocumentService: TextDocumentService = QuarkdownTextDocumentService(this)
    private val workspaceService: WorkspaceService = QuarkdownWorkspaceService(this)

    private lateinit var client: LanguageClient

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
        val response =
            InitializeResult(ServerCapabilities())
                .apply {
                    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
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
