package com.quarkdown.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

/**
 *
 */
class QuarkdownLanguageServer : LanguageServer {
    private val textDocumentService: TextDocumentService = QuarkdownTextDocumentService(this)
    private val workspaceService: WorkspaceService = QuarkdownWorkspaceService()

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
}
