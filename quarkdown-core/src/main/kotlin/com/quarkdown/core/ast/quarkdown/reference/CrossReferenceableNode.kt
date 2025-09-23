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
}
