package com.quarkdown.lsp

import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.lsp.documentation.HtmlToMarkdown
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
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

    private fun emptyCompletion(): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? =
        CompletableFuture.completedFuture(Either.forRight(CompletionList(false, emptyList())))

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem?>?, CompletionList?>?>? {
        if (server.docsDirectory == null) {
            server.log("No documentation directory found, cannot provide completions.")
            return emptyCompletion()
        }

        val text =
            getDocumentText(params.textDocument)
                ?: return emptyCompletion()

        val line =
            text.lines().getOrNull(params.position.line)
                ?: return emptyCompletion()

        val offset = params.position.character

        val isFunctionCall = line.getOrNull(offset - 1)?.toString() == FunctionCallGrammar.BEGIN

        if (!isFunctionCall) {
            return emptyCompletion()
        }

        return CompletableFuture.supplyAsync {
            server.log("Operation '" + "text/completion")

            val walker = DokkaHtmlWalker(server.docsDirectory!!)

            val completions: List<CompletionItem> =
                walker
                    .walk()
                    .filter { it.isInModule }
                    .map { function ->
                        val completionItem = CompletionItem()
                        completionItem.label = function.name
                        completionItem.insertText = function.name
                        completionItem.detail = function.moduleName
                        completionItem.kind = CompletionItemKind.Function

                        completionItem.documentation =
                            function.extractor().extractContent()?.let {
                                val md = HtmlToMarkdown.convert(it)
                                Either.forRight(MarkupContent(MarkupKind.MARKDOWN, md))
                            }
                        completionItem
                    }.toList()

            Either.forLeft(completions)
        }
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> =
        CompletableFuture.completedFuture(unresolved)
}
