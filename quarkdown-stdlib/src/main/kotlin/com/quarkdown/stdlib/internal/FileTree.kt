package com.quarkdown.stdlib.internal

import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.ast.quarkdown.block.FileTreeEntry
import com.quarkdown.core.util.node.conversion.list.MarkdownListToList
import com.quarkdown.core.util.node.toPlainText

/**
 * Text patterns that produce a [FileTreeEntry.Ellipsis] entry,
 * indicating omitted content in the file tree.
 * Includes both the raw ASCII `...` and the Unicode ellipsis `…` (U+2026),
 */
private val ELLIPSIS_TEXTS = setOf("...", "\u2026")

/**
 * Recursively converts a Markdown [ListBlock] into a flat list of [FileTreeEntry] elements.
 * Inline items become [FileTreeEntry.File]s, nested items become [FileTreeEntry.Directory]s,
 * and items with `...` as text become [FileTreeEntry.Ellipsis].
 */
internal fun fileTreeFromList(list: ListBlock): List<FileTreeEntry> =
    MarkdownListToList(
        list,
        inlineValueMapper = { node ->
            val text = listOf(node).toPlainText()
            if (text in ELLIPSIS_TEXTS) FileTreeEntry.Ellipsis else FileTreeEntry.File(text)
        },
        nestedValueMapper = { parent, nestedList ->
            FileTreeEntry.Directory(
                listOf(parent).toPlainText(),
                fileTreeFromList(nestedList),
            )
        },
    ).convert()
