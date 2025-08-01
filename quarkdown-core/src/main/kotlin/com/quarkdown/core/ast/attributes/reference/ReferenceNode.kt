package com.quarkdown.core.ast.attributes.reference

import com.quarkdown.core.ast.Node
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext

/** Represents a node that may reference some definition that is generated by elsewhere in the document.
 *
 * Examples:
 * - [com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation] refers to a [com.quarkdown.core.bibliography.BibliographyEntry]
 * - [com.quarkdown.core.ast.base.inline.ReferenceFootnote] refers to a [com.quarkdown.core.ast.base.block.FootnoteDefinition]
 *
 * @param R the type of the reference element
 * @param D the type of the definition associated with the reference
 */
interface ReferenceNode<R, D> : Node {
    /**
     * The reference element to associate with the definition.
     */
    val reference: R
}

/**
 * @param context context where the [ResolvedReferenceProperty] is stored
 * @return the definition associated with [this] reference within the document handled by [context],
 * or `null` if the definition for [this] node is not registered or resolved
 */
fun <R, D> ReferenceNode<R, D>.getDefinition(context: Context): D? =
    context.attributes
        .of(this)[ResolvedReferenceProperty.Key<R, D>()]
        ?.second

/**
 * Registers the given [definition] as the definition associated with [this] reference within the document handled by [context].
 * @param context context where the [ResolvedReferenceProperty] is stored
 * @param definition the definition to associate with [this] reference
 * @see com.quarkdown.core.context.hooks.reference.ReferenceDefinitionResolverHook for the assignment stage
 */
fun <R, D> ReferenceNode<R, D>.setDefinition(
    context: MutableContext,
    definition: D,
) {
    context.attributes.of(this) += ResolvedReferenceProperty(this.reference to definition)
}
