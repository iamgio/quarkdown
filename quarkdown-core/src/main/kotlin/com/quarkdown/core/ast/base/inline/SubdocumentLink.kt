package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A link to a Quarkdown subdocument.
 * @param link the link to the subdocument
 */
class SubdocumentLink(
    val link: Link,
) : LinkNode by link,
    TextNode by link {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
