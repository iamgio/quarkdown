package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.property.Property
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

/**
 * A property that holds a reference to a [Subdocument] associated with a [SubdocumentLink]
 * during the tree traversal stage.
 * @see com.quarkdown.core.context.hooks.SubdocumentRegistrationHook for the registration stage
 */
data class SubdocumentProperty(
    override val value: Subdocument,
) : Property<Subdocument> {
    companion object : Property.Key<Subdocument>

    override val key: Property.Key<Subdocument> = SubdocumentProperty
}

/**
 * @returns the [Subdocument] associated with this [SubdocumentLink] in the given [context], if any
 */
fun SubdocumentLink.getSubdocument(context: Context): Subdocument? = context.attributes.of(this)[SubdocumentProperty]

/**
 * Associates a [Subdocument] with the [SubdocumentLink] in the given [context].
 * @param context context where subdocument data is stored
 * @param subdocument the subdocument to set
 * @see com.quarkdown.core.context.hooks.SubdocumentRegistrationHook for the registration stage
 */
fun SubdocumentLink.setSubdocument(
    context: MutableContext,
    subdocument: Subdocument,
) {
    context.attributes.of(this) += SubdocumentProperty(subdocument)
}
