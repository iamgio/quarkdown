package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.block.BlockRecognizer
import com.quarkdown.core.lexer.scan.block.Interruption

/**
 * The block recognizers of the base Markdown flavor, highest priority first.
 * @param paragraph what interrupts a paragraph, a block quote and a footnote definition
 * @param tableRows what interrupts a table's rows
 * @param list what interrupts a list
 * @return the recognizers, in the order they are tried
 */
fun baseBlockRecognizers(
    paragraph: Interruption,
    tableRows: Interruption,
    list: Interruption,
): List<BlockRecognizer> =
    listOf(
        CommentRecognizer,
        BlockQuoteRecognizer(paragraph),
        IndentedCodeRecognizer,
        FootnoteDefinitionRecognizer(paragraph),
        LinkDefinitionRecognizer,
        FencedCodeRecognizer,
        HeadingRecognizer,
        ThematicBreakRecognizer,
        SetextHeadingRecognizer,
        TableRecognizer(tableRows),
        ListRecognizer(list, ListKind.UNORDERED),
        ListRecognizer(list, ListKind.ORDERED),
        NewlineRecognizer,
        ParagraphRecognizer(paragraph),
        BlockTextRecognizer,
    )
