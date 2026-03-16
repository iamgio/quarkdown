package com.quarkdown.stdlib.internal

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
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
 * Checks whether a node tree contains a [Strong] or [StrongEmphasis] node at any depth,
 * indicating that the entry should be highlighted.
 */
private fun Node.isHighlighted(): Boolean =
    this is Strong ||
        this is StrongEmphasis ||
        (this is NestableNode && children.any { it.isHighlighted() })

/**
 * Recursively converts a Markdown [ListBlock] into a flat list of [FileTreeEntry] elements.
 * Inline items become [FileTreeEntry.File]s, nested items become [FileTreeEntry.Directory]s,
 * and items with `...` as text become [FileTreeEntry.Ellipsis].
 * Entries wrapped in strong emphasis are marked as highlighted.
 */
internal fun fileTreeFromList(list: ListBlock): List<FileTreeEntry> =
    MarkdownListToList(
        list,
        inlineValueMapper = { node ->
            val text = listOf(node).toPlainText()
            val highlighted = node.isHighlighted()
            if (text in ELLIPSIS_TEXTS) {
                FileTreeEntry.Ellipsis(highlighted)
            } else {
                FileTreeEntry.File(text, highlighted)
            }
        },
        nestedValueMapper = { parent, nestedList ->
            FileTreeEntry.Directory(
                listOf(parent).toPlainText(),
                fileTreeFromList(nestedList),
                highlighted = parent.isHighlighted(),
            )
        },
    ).convert()
