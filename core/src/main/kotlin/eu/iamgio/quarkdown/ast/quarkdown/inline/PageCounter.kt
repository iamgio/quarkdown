package eu.iamgio.quarkdown.ast.quarkdown.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A counter for the current or total page number.
 * In case the current document type does not support page counting (e.g. plain document),
 * a placeholder is used at rendering time.
 * @param target whether the counter should display the current or total page number
 */
data class PageCounter(val target: Target) : Node {
    enum class Target {
        /**
         * The current page number.
         */
        CURRENT,

        /**
         * The total amount of pages.
         */
        TOTAL,
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
