package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An immutable checkbox that is either checked or unchecked.
 * @param isChecked whether the checkbox is checked
 * @see eu.iamgio.quarkdown.ast.base.block.TaskListItem
 */
data class CheckBox(
    val isChecked: Boolean,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
