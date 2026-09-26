package com.quarkdown.lsp.ontype

import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.TextPatch
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.util.getLine
import com.quarkdown.lsp.util.toOffset

/**
 * Formatter that, when the user presses Enter after a line ending with `\` (line continuation)
 * inside a function call, inserts indentation on the new line to align with the first argument.
 *
 * For example, given:
 * ```
 * .container alignment:{center} \
 * ```
 * After pressing Enter, the new line is indented to:
 * ```
 * .container alignment:{center} \
 *            <cursor here>
 * ```
 */
class LineContinuationAutoIndentOnTypeFormattingEditSupplier : OnTypeFormattingEditSupplier {
    override fun getEdits(
        position: CursorPosition,
        document: TextDocument,
    ): List<TextPatch> {
        val text = document.text
        val previousLineNum = position.line - 1
        val previousLine = text.getLine(previousLineNum) ?: return emptyList()

        if (!previousLine.trimEnd().endsWith(FunctionCallGrammar.LINE_CONTINUATION)) {
            return emptyList()
        }

        // Only act on the first continuation line. Subsequent ones are already indented by the editor's built-in auto-indent.
        val lineBeforePrevious = text.getLine(previousLineNum - 1)
        if (lineBeforePrevious?.trimEnd()?.endsWith(FunctionCallGrammar.LINE_CONTINUATION) == true) {
            return emptyList()
        }

        // Look up the function call before the `\`, since the `\` itself may fall outside
        // the parsed call range when the next line has no content yet.
        val searchColumn =
            previousLine
                .trimEnd()
                .dropLast(1)
                .trimEnd()
                .length - 1
        if (searchColumn < 0) return emptyList()

        val call: FunctionCall =
            document.functionCalls
                .getAtSourceIndex(CursorPosition(previousLineNum, searchColumn).toOffset(text))
                ?: return emptyList()

        val indentation = call.argumentStartColumn(text)

        return listOf(
            TextPatch(
                start = CursorPosition(position.line, 0),
                end = CursorPosition(position.line, 0),
                text = " ".repeat(indentation),
            ),
        )
    }

    /**
     * @return the column where arguments begin, i.e. right after `.functionname `.
     */
    private fun FunctionCall.argumentStartColumn(text: String): Int {
        val callStartOffset = range.first
        val lineStartOffset = text.lastIndexOf('\n', callStartOffset - 1) + 1
        val callColumn = callStartOffset - lineStartOffset

        val prefixLength =
            tokens
                .takeWhile { it.type == FunctionCallToken.Type.BEGIN || it.type == FunctionCallToken.Type.FUNCTION_NAME }
                .sumOf { it.lexeme.length }

        return callColumn + prefixLength + 1 // +1 for the space after the function name.
    }
}
