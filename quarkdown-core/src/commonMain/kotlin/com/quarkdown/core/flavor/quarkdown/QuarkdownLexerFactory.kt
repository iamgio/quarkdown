package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.flavor.InlineLexerVariant
import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.flavor.base.BaseMarkdownLexerFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.scan.block.BlockScanLexer
import com.quarkdown.core.lexer.scan.block.QuarkdownInterruptions
import com.quarkdown.core.lexer.scan.block.base.baseBlockRecognizers
import com.quarkdown.core.lexer.scan.block.quarkdown.withQuarkdownBlockExtensions
import com.quarkdown.core.lexer.scan.inline.InlineScanLexer
import com.quarkdown.core.lexer.scan.inline.base.EscapeRecognizer
import com.quarkdown.core.lexer.scan.inline.base.baseInlineRecognizers
import com.quarkdown.core.lexer.scan.inline.quarkdown.ExpressionFunctionCallRecognizer
import com.quarkdown.core.lexer.scan.inline.quarkdown.InlineFunctionCallRecognizer
import com.quarkdown.core.lexer.scan.inline.quarkdown.withQuarkdownInlineExtensions
import com.quarkdown.core.lexer.tokens.PlainTextToken

/**
 * [QuarkdownFlavor] lexer factory.
 */
object QuarkdownLexerFactory : LexerFactory {
    private val base = BaseMarkdownLexerFactory

    private val blockRecognizers =
        baseBlockRecognizers(
            QuarkdownInterruptions.paragraph,
            QuarkdownInterruptions.tableRows,
            QuarkdownInterruptions.list,
        ).withQuarkdownBlockExtensions()

    private val inlineRecognizers =
        InlineLexerVariant.entries.associateWith { baseInlineRecognizers(it).withQuarkdownInlineExtensions() }

    private val expressionRecognizers =
        mapOf(
            true to listOf(EscapeRecognizer, ExpressionFunctionCallRecognizer, InlineFunctionCallRecognizer),
            false to listOf(EscapeRecognizer, InlineFunctionCallRecognizer),
        )

    override fun newBlockLexer(source: CharSequence): Lexer = BlockScanLexer(source, blockRecognizers)

    override fun newListLexer(source: CharSequence): Lexer = base.newListLexer(source)

    override fun newInlineLexer(
        source: CharSequence,
        variant: InlineLexerVariant,
    ): Lexer = InlineScanLexer(source, inlineRecognizers.getValue(variant), fill = ::PlainTextToken)

    override fun newExpressionLexer(
        source: CharSequence,
        allowBlockFunctionCalls: Boolean,
    ): Lexer = InlineScanLexer(source, expressionRecognizers.getValue(allowBlockFunctionCalls), fill = ::PlainTextToken)

    /**
     * Creates a lexer for inline function calls.
     * This lexer is mainly used for function call completion and highlighting in the LSP.
     * @param source the source text to tokenize
     * @return a lexer that recognizes inline function calls
     *         (block arguments are not included, as they are part of block function calls)
     */
    fun newInlineFunctionCallLexer(source: CharSequence): Lexer = InlineScanLexer(source, listOf(InlineFunctionCallRecognizer), fill = null)
}
