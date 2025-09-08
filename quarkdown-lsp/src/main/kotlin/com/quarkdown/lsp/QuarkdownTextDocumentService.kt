package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.hover.HoverSupplier
import com.quarkdown.lsp.ontype.OnTypeFormattingEditSupplier
import com.quarkdown.lsp.subservices.CompletionSubservice
import com.quarkdown.lsp.subservices.DiagnosticsSubservice
import com.quarkdown.lsp.subservices.HoverSubservice
import com.quarkdown.lsp.subservices.OnTypeFormattingSubservice
import com.quarkdown.lsp.subservices.SemanticTokensSubservice
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

private typealias CompletionResult = CompletableFuture<Either<List<CompletionItem>, CompletionList>>

/**
 * Service for handling text document operations in the Quarkdown Language Server.
 */
class QuarkdownTextDocumentService(
    private val server: QuarkdownLanguageServer,
    completionSuppliers: List<CompletionSupplier>,
    tokensSuppliers: List<SemanticTokensSupplier>,
    hoverSuppliers: List<HoverSupplier>,
    diagnosticsSuppliers: List<DiagnosticsSupplier>,
    formattingSuppliers: List<OnTypeFormattingEditSupplier>,
) : TextDocumentService {
    private val completionService = CompletionSubservice(completionSuppliers)
    private val semanticTokensService = SemanticTokensSubservice(tokensSuppliers)
    private val hoverService = HoverSubservice(hoverSuppliers)
    private val diagnosticsService = DiagnosticsSubservice(diagnosticsSuppliers)
    private val onTypeFormattingService = OnTypeFormattingSubservice(formattingSuppliers)

    /**
     * Maps document URIs to their text content.
     */
    private val documents = mutableMapOf<String, TextDocument>()

    /**
     * Adds or updates a document in the internal URI association map,
     * and triggers async diagnostics processing.
     * @param uri the URI of the document
     * @param text the text content of the document
     * @param invalidateCache whether to invalidate any cached data associated with the document
     */
    private fun putDocument(
        uri: String,
        text: CharSequence,
        invalidateCache: Boolean = false,
    ) {
        // Line endings are normalized to LF to ensure consistency.
        val text = text.normalizeLineSeparators().toString()
        val current: TextDocument? = documents[uri]

        val new: TextDocument =
            current
                ?.copy(text = text, cache = if (invalidateCache) null else current.cache)
                ?: TextDocument(
                    text = text,
                    setActive = { documents[uri] = this },
                )

        documents[uri] = new

        thread { processDiagnostics(uri, new) }
    }

    /**
     * Processes diagnostics (warning, errors, etc.) for the given document and publishes them to the client.
     * @param uri the URI of the document
     * @param document the document to process
     */
    private fun processDiagnostics(
        uri: String,
        document: TextDocument,
    ) {
        val diagnostics: List<Diagnostic> = diagnosticsService.process(params = null, document)
        server.publishDiagnostics(uri, diagnostics)
    }

    /**
     * @return the document associated with the given identifier
     */
    private fun getDocument(document: TextDocumentIdentifier): TextDocument =
        documents[document.uri]
            ?: throw IllegalArgumentException("No document found for URI: ${document.uri}")

    override fun didOpen(didOpenTextDocumentParams: DidOpenTextDocumentParams) {
        server.log(
            "Operation 'text/didOpen'" +
                "' {fileUri: '" + didOpenTextDocumentParams.textDocument.uri + "'} opened",
        )
        putDocument(didOpenTextDocumentParams.textDocument.uri, didOpenTextDocumentParams.textDocument.text)
    }

    override fun didChange(didChangeTextDocumentParams: DidChangeTextDocumentParams) {
        server.log("Operation 'text/didChange'")

        putDocument(
            didChangeTextDocumentParams.textDocument.uri,
            didChangeTextDocumentParams.contentChanges
                .firstOrNull()
                ?.text
                ?.normalizeLineSeparators()
                ?: "",
            invalidateCache = true,
        )
    }

    override fun didClose(didCloseTextDocumentParams: DidCloseTextDocumentParams) {
        server.log("Operation 'text/didClose'")

        documents.remove(didCloseTextDocumentParams.textDocument.uri)
    }

    override fun didSave(didSaveTextDocumentParams: DidSaveTextDocumentParams) {
        server.log("Operation 'text/didSave'")
    }

    override fun completion(params: CompletionParams): CompletionResult {
        val document = getDocument(params.textDocument)

        return CompletableFuture.supplyAsync {
            server.log("Operation 'text/completion'")
            Either.forLeft(completionService.process(params, document))
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> =
        CompletableFuture.completedFuture(unresolved)

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        server.log("Operation 'text/semanticTokens/full'")

        val document = getDocument(params.textDocument)
        return CompletableFuture.completedFuture(semanticTokensService.process(params, document))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover?>? {
        server.log("Operation 'text/hover'")

        val document = getDocument(params.textDocument)
        return CompletableFuture.completedFuture(hoverService.process(params, document))
    }

    override fun onTypeFormatting(params: DocumentOnTypeFormattingParams): CompletableFuture<List<TextEdit?>?>? {
        server.log("Operation 'text/onTypeFormatting'")

        val document = getDocument(params.textDocument)
        return CompletableFuture.completedFuture(onTypeFormattingService.process(params, document))
    }
}
