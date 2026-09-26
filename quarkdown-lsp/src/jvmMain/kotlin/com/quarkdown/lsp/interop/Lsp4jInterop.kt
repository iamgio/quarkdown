package com.quarkdown.lsp.interop

import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CompletionKind
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.HoverInfo
import com.quarkdown.lsp.model.Severity
import com.quarkdown.lsp.model.TextPatch
import com.quarkdown.lsp.util.offsetToPosition
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

/**
 * @return [this] LSP4J position as a [CursorPosition]
 */
fun Position.toCursorPosition(): CursorPosition = CursorPosition(line, character)

/**
 * @return [this] position as an LSP4J [Position]
 */
fun CursorPosition.toLsp4j(): Position = Position(line, column)

/**
 * @return [this] Markdown content as LSP4J [MarkupContent]
 */
fun String.toMarkdownMarkup(): MarkupContent = MarkupContent(MarkupKind.MARKDOWN, this)

/**
 * @return [this] completion as an LSP4J [CompletionItem]
 */
fun Completion.toLsp4j(): CompletionItem =
    CompletionItem().also {
        it.label = label
        it.detail = detail
        it.documentation = documentationMarkdown?.let { markdown -> Either.forRight(markdown.toMarkdownMarkup()) }
        it.kind = kind.toLsp4j()
        it.insertTextFormat = InsertTextFormat.Snippet
        it.insertText = insertionSnippet
    }

private fun CompletionKind.toLsp4j(): CompletionItemKind =
    when (this) {
        CompletionKind.FUNCTION -> CompletionItemKind.Function
        CompletionKind.PARAMETER -> CompletionItemKind.Field
        CompletionKind.VALUE -> CompletionItemKind.Value
    }

/**
 * @return [this] hover information as an LSP4J [Hover]
 */
fun HoverInfo.toLsp4j(): Hover = Hover(contentMarkdown.toMarkdownMarkup())

/**
 * @return [this] patch as an LSP4J [TextEdit]
 */
fun TextPatch.toLsp4j(): TextEdit = TextEdit(Range(start.toLsp4j(), end.toLsp4j()), text)

/**
 * @param text the text of the document, used to convert offsets to positions
 * @return [this] diagnostic as an LSP4J [Diagnostic]
 */
fun SimpleDiagnostic.toLsp4j(text: String): Diagnostic =
    Diagnostic().also {
        it.range = Range(offsetToPosition(text, range.first).toLsp4j(), offsetToPosition(text, range.last).toLsp4j())
        it.message = Either.forLeft(message)
        it.severity = severity.toLsp4j()
    }

private fun Severity.toLsp4j(): DiagnosticSeverity =
    when (this) {
        Severity.WARNING -> DiagnosticSeverity.Warning
        Severity.ERROR -> DiagnosticSeverity.Error
    }
