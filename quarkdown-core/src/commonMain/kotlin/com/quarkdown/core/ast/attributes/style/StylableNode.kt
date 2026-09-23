package com.quarkdown.core.ast.attributes.style

import com.quarkdown.core.ast.Node

/**
 * A node that can be styled after a [NodeStyle] attribute.
 */
interface StylableNode : Node {
    /**
     * The style of this node.
     */
    val style: NodeStyle
}
