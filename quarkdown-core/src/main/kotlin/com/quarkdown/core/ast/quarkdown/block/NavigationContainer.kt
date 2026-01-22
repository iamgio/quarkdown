package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creates a navigation container, which marks its content as a navigable section.
 *
 * This doesn't affect the layout of the document by itself, but can be used by themes and renderers
 * to provide additional navigation features, styling, behaviors and accessibility.
 */
class NavigationContainer(
    val role: Role? = null,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * Role of the [NavigationContainer], indicating its purpose in the document.
     *
     * Roles improve accessibility, and enable specific features in renderers and themes.
     */
    enum class Role : RenderRepresentable {
        TABLE_OF_CONTENTS,
        PAGE_LIST,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
