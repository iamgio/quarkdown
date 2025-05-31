package com.quarkdown.lsp

import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 *
 */
class QuarkdownTextDocumentService(
    private val languageServer: QuarkdownLanguageServer,
) : TextDocumentService {
    override fun didOpen(didOpenTextDocumentParams: DidOpenTextDocumentParams) {
        println(
            "Operation '" + "text/didOpen" +
                "' {fileUri: '" + didOpenTextDocumentParams.textDocument.uri + "'} opened",
        )
    }

    override fun didChange(didChangeTextDocumentParams: DidChangeTextDocumentParams) {
        println(
            "Operation '" + "text/didChange" +
                "' {fileUri: '" + didChangeTextDocumentParams.textDocument.uri + "'} Changed",
        )
    }

    override fun didClose(didCloseTextDocumentParams: DidCloseTextDocumentParams) {
        println(
            "Operation '" + "text/didClose" +
                "' {fileUri: '" + didCloseTextDocumentParams.textDocument.uri + "'} Closed",
        )
    }

    override fun didSave(didSaveTextDocumentParams: DidSaveTextDocumentParams) {
        println(
            "Operation '" + "text/didSave" +
                "' {fileUri: '" + didSaveTextDocumentParams.textDocument.uri + "'} Saved",
        )
    }

    override fun completion(position: CompletionParams?): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? =
        CompletableFuture.supplyAsync(
            Supplier {
                println("Operation '" + "text/completion")
                val completionItem = CompletionItem()
                completionItem.label = "Test completion item"
                completionItem.insertText = "Test"
                completionItem.detail = "Snippet"
                completionItem.kind = CompletionItemKind.Snippet
                Either.forLeft(listOf<CompletionItem?>(completionItem))
            },
        )
}
