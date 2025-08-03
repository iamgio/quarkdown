package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A graph representing the relationships between [com.quarkdown.core.document.sub.Subdocument]s
 * within the document, stored in [com.quarkdown.core.context.Context.subdocumentGraph].
 */
class SubdocumentGraph : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
