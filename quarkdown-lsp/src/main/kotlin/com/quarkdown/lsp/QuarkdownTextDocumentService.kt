package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.hover.HoverSupplier
import com.quarkdown.lsp.subservices.CompletionSubservice
import com.quarkdown.lsp.subservices.HoverSubservice
import com.quarkdown.lsp.subservices.SemanticTokensSubservice
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

private typealias CompletionResult = CompletableFuture<Either<List<CompletionItem>, CompletionList>>

/**
 * Service for handling text document operations in the Quarkdown Language Server.
 */
class QuarkdownTextDocumentService(
    private val server: QuarkdownLanguageServer,
    completionSuppliers: List<CompletionSupplier>,
    tokensSuppliers: List<SemanticTokensSupplier>,
    hoverSuppliers: List<HoverSupplier>,
) : TextDocumentService {
    private val completionService = CompletionSubservice(completionSuppliers)
    private val semanticTokensService = SemanticTokensSubservice(tokensSuppliers)
    private val hoverService = HoverSubservice(hoverSuppliers)

    /**
     * Maps document URIs to their text content.
     */
    private val documents = mutableMapOf<String, String>()

    private fun getDocumentText(document: TextDocumentIdentifier): String =
        documents[document.uri]
            ?: throw IllegalArgumentException("No document found for URI: ${document.uri}")

    override fun didOpen(didOpenTextDocumentParams: DidOpenTextDocumentParams) {
        server.log(
            "Operation 'text/didOpen'" +
                "' {fileUri: '" + didOpenTextDocumentParams.textDocument.uri + "'} opened",
        )

        // The text is stored and line endings are normalized to LF to ensure consistency with the protocol.
        documents[didOpenTextDocumentParams.textDocument.uri] =
            didOpenTextDocumentParams.textDocument.text
                .normalizeLineSeparators()
                .toString()
    }

    override fun didChange(didChangeTextDocumentParams: DidChangeTextDocumentParams) {
        server.log(
            "Operation 'text/didChange'" +
                " {fileUri: '" + didChangeTextDocumentParams.textDocument.uri + "'} Changed",
        )

        documents[didChangeTextDocumentParams.textDocument.uri] =
            didChangeTextDocumentParams.contentChanges.firstOrNull()?.text ?: ""
    }

    override fun didClose(didCloseTextDocumentParams: DidCloseTextDocumentParams) {
        server.log(
            "Operation 'text/didClose'" +
                "' {fileUri: '" + didCloseTextDocumentParams.textDocument.uri + "'} Closed",
        )

        documents.remove(didCloseTextDocumentParams.textDocument.uri)
    }

    override fun didSave(didSaveTextDocumentParams: DidSaveTextDocumentParams) {
        server.log(
            "Operation '" + "text/didSave" +
                "' {fileUri: '" + didSaveTextDocumentParams.textDocument.uri + "'} Saved",
        )
    }

    override fun completion(params: CompletionParams): CompletionResult {
        val text = getDocumentText(params.textDocument)

        return CompletableFuture.supplyAsync {
            server.log("Operation 'text/completion'")
            Either.forLeft(completionService.process(params, text))
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> =
        CompletableFuture.completedFuture(unresolved)

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        val text = getDocumentText(params.textDocument)
        server.log("Operation 'text/semanticTokens/full'")

        return CompletableFuture.completedFuture(semanticTokensService.process(params, text))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?>? {
        val text = getDocumentText(params.textDocument)
        server.log("Operation 'text/hover'")

        return CompletableFuture.completedFuture(hoverService.process(params, text))
    }
}
