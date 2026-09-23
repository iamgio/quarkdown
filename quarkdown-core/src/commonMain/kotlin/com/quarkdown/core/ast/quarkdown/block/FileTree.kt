package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A visual representation of a file system hierarchy, composed of [FileTreeEntry] elements.
 * @param entries top-level entries of the file tree
 * @see FileTreeEntry
 */
class FileTree(
    val entries: List<FileTreeEntry>,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

/**
 * A single entry in a [FileTree].
 */
sealed interface FileTreeEntry {
    /**
     * Whether this entry is visually highlighted (e.g. rendered in bold).
     * An entry is highlighted when its text is wrapped in strong emphasis (`**name**`).
     */
    val highlighted: Boolean

    /**
     * A leaf entry representing a file.
     * @param name file name, including extension
     * @param highlighted whether this file should be visually highlighted
     */
    data class File(
        val name: String,
        override val highlighted: Boolean = false,
    ) : FileTreeEntry

    /**
     * A branch entry representing a directory, which contains nested [entries].
     * @param name directory name
     * @param entries children of this directory (files and subdirectories)
     * @param highlighted whether this directory should be visually highlighted
     */
    data class Directory(
        val name: String,
        val entries: List<FileTreeEntry>,
        override val highlighted: Boolean = false,
    ) : FileTreeEntry

    /**
     * A placeholder entry indicating omitted content, created by a `- ...` item.
     * @param highlighted whether this ellipsis should be visually highlighted
     */
    data class Ellipsis(
        override val highlighted: Boolean = false,
    ) : FileTreeEntry
}
