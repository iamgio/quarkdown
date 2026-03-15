package com.quarkdown.stdlib

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.stdlib.internal.fileTreeFromList

/**
 * `MiscElements` stdlib module exporter.
 * This module handles miscellaneous elements that do not fit in other modules.
 */
val MiscElements: QuarkdownModule =
    moduleOf(
        ::fileTree,
    )

/**
 * Creates a visual file tree from a Markdown list.
 * Each inline item is rendered as a file, and each nested list as a directory.
 *
 * Example:
 * ```
 * .filetree
 *     - src
 *       - main.ts
 *       - utils.ts
 *     - README.md
 * ```
 * @param content body content containing a Markdown list that defines the file tree structure
 * @return the generated [FileTree] node
 */
@Name("filetree")
fun fileTree(
    @LikelyBody content: MarkdownContent,
): NodeValue {
    val rawListNode =
        content.children.firstOrNull() as? ListBlock
            ?: throw IllegalArgumentException("Content of file tree must be a list.")

    return FileTree(fileTreeFromList(rawListNode)).wrappedAsValue()
}
