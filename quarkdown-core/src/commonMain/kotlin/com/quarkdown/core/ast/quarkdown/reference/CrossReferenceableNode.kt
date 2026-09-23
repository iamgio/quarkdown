package com.quarkdown.core.ast.quarkdown.reference

import com.quarkdown.core.ast.Node

/**
 * A node that can be referenced by a [CrossReference], by means of matching [referenceId]s.
 */
interface CrossReferenceableNode : Node {
    /**
     * The ID used to reference this node.
     * If `null`, this node cannot be referenced.
     * In order to be referenced by a [CrossReference], this ID must match the ID of the [CrossReference].
     */
    val referenceId: String?

    companion object {
        /**
         * Conventional placeholder reference ID used to enable numbering on a node
         * without making it cross-referenceable (e.g. `$ E = mc^2 $ {#_}`).
         */
        const val PLACEHOLDER_REFERENCE_ID = "_"
    }
}

/**
 * The [CrossReferenceableNode.referenceId] of this node, if it is a real linkable ID
 * (i.e. not the [placeholder][CrossReferenceableNode.PLACEHOLDER_REFERENCE_ID]).
 */
val CrossReferenceableNode.linkableReferenceId: String?
    get() = referenceId?.takeUnless { it == CrossReferenceableNode.PLACEHOLDER_REFERENCE_ID }
