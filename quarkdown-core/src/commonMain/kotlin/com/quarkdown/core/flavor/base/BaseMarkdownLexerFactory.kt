package com.quarkdown.core.flavor.base

import com.quarkdown.core.flavor.InlineLexerVariant
import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.scan.block.BlockScanLexer
import com.quarkdown.core.lexer.scan.block.base.ListItemRecognizer
import com.quarkdown.core.lexer.scan.block.base.NewlineRecognizer
import com.quarkdown.core.lexer.scan.block.base.baseBlockRecognizers
import com.quarkdown.core.lexer.scan.inline.InlineScanLexer
import com.quarkdown.core.lexer.scan.inline.base.baseInlineRecognizers
import com.quarkdown.core.lexer.tokens.PlainTextToken

/**
 * [BaseMarkdownFlavor] lexer factory.
 */
object BaseMarkdownLexerFactory : LexerFactory {
    private val blockRecognizers =
        baseBlockRecognizers(BaseInterruptions.paragraph, BaseInterruptions.tableRows, BaseInterruptions.list)

    private val listRecognizers = listOf(ListItemRecognizer, NewlineRecognizer)

    private val inlineRecognizers = InlineLexerVariant.entries.associateWith(::baseInlineRecognizers)

    override fun newBlockLexer(source: CharSequence): Lexer = BlockScanLexer(source, blockRecognizers)

    override fun newListLexer(source: CharSequence): Lexer = BlockScanLexer(source, listRecognizers)

    override fun newInlineLexer(
        source: CharSequence,
        variant: InlineLexerVariant,
    ): Lexer = InlineScanLexer(source, inlineRecognizers.getValue(variant), fill = ::PlainTextToken)

    // Functions aren't supported by this flavor
    override fun newExpressionLexer(
        source: CharSequence,
        allowBlockFunctionCalls: Boolean,
    ): Lexer = InlineScanLexer(source, recognizers = emptyList(), fill = null)
}
