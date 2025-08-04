package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.highlight.SemanticTokenData
import com.quarkdown.lsp.highlight.SemanticTokensEncoder
import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.highlight.toSemanticData
import com.quarkdown.lsp.hover.HoverSupplier
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
    private val completionSuppliers: List<CompletionSupplier>,
    private val hoverSuppliers: List<HoverSupplier>,
    private val tokensSuppliers: List<SemanticTokensSupplier>,
) : TextDocumentService {
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

        documents[didOpenTextDocumentParams.textDocument.uri] =
            didOpenTextDocumentParams.textDocument.text
    }

    override fun didChange(didChangeTextDocumentParams: DidChangeTextDocumentParams) {
        server.log(
            "Operation 'text/didChange'" +
                "' {fileUri: '" + didChangeTextDocumentParams.textDocument.uri + "'} Changed",
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

            val completions =
                completionSuppliers
                    .flatMap { it.getCompletionItems(params, text) }

            Either.forLeft(completions)
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> =
        CompletableFuture.completedFuture(unresolved)

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        server.log("Operation 'text/semanticTokens/full'")

        // Lexers replace line endings with LF, so it's normalized here too to ensure consistency.
        val text = getDocumentText(params.textDocument).normalizeLineSeparators().toString()

        val tokens: List<SemanticTokenData> =
            this.tokensSuppliers
                .flatMap { it.getTokens(params, text) }
                .map { it.toSemanticData(text) }

        val encoded = SemanticTokensEncoder.encode(tokens)
        val result = SemanticTokens(encoded)

        return CompletableFuture.completedFuture(result)
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?>? {
        val text = getDocumentText(params.textDocument)

        server.log("Operation '" + "text/hover")

        val hover =
            hoverSuppliers
                .asSequence()
                .mapNotNull { it.getHover(params, text) }
                .firstOrNull()
                ?: return CompletableFuture.completedFuture(null)

        return CompletableFuture.completedFuture(hover)
    }
}
