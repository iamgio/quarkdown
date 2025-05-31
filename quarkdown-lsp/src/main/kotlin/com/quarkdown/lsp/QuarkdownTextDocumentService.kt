package com.quarkdown.lsp

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.stdlib.Stdlib
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

/**
 *
 */
class QuarkdownTextDocumentService(
    private val server: QuarkdownLanguageServer,
) : TextDocumentService {
    /**
     * Maps document URIs to their text content.
     */
    private val documents = mutableMapOf<String, String>()

    override fun didOpen(didOpenTextDocumentParams: DidOpenTextDocumentParams) {
        server.log(
            "Operation '" + "text/didOpen" +
                "' {fileUri: '" + didOpenTextDocumentParams.textDocument.uri + "'} opened",
        )

        documents[didOpenTextDocumentParams.textDocument.uri] =
            didOpenTextDocumentParams.textDocument.text
    }

    override fun didChange(didChangeTextDocumentParams: DidChangeTextDocumentParams) {
        server.log(
            "Operation '" + "text/didChange" +
                "' {fileUri: '" + didChangeTextDocumentParams.textDocument.uri + "'} Changed",
        )

        documents[didChangeTextDocumentParams.textDocument.uri] =
            didChangeTextDocumentParams.contentChanges.firstOrNull()?.text ?: ""
    }

    override fun didClose(didCloseTextDocumentParams: DidCloseTextDocumentParams) {
        server.log(
            "Operation '" + "text/didClose" +
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

    private fun getDocumentText(document: TextDocumentIdentifier): String? =
        documents[document.uri] ?: throw IllegalArgumentException("No document found for URI: ${document.uri}")

    override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? =
        CompletableFuture.supplyAsync {
            val libraries: Set<Library> = LibraryExporter.exportAll(Stdlib)

            server.log("Operation '" + "text/completion")

            val completions: List<CompletionItem> =
                libraries.flatMap { it.functions }.map { function ->
                    val completionItem = CompletionItem()
                    completionItem.label = function.name
                    completionItem.insertText = function.name
                    completionItem.detail = "Function"
                    completionItem.kind = CompletionItemKind.Function
                    completionItem
                }

            Either.forLeft(completions)
        }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> =
        CompletableFuture.completedFuture(unresolved)
}
