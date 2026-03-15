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
     * A leaf entry representing a file.
     * @param name file name, including extension
     */
    data class File(
        val name: String,
    ) : FileTreeEntry

    /**
     * A branch entry representing a directory, which contains nested [entries].
     * @param name directory name
     * @param entries children of this directory (files and subdirectories)
     */
    data class Directory(
        val name: String,
        val entries: List<FileTreeEntry>,
    ) : FileTreeEntry

    /**
     * A placeholder entry indicating omitted content, created by a `- ...` item.
     */
    data object Ellipsis : FileTreeEntry
}
