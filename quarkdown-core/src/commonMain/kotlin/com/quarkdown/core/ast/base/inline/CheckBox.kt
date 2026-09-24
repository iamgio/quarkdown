package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An immutable checkbox that is either checked or unchecked.
 * @param isChecked whether the checkbox is checked
 * @see com.quarkdown.core.ast.base.block.TaskListItem
 */
class CheckBox(
    val isChecked: Boolean,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
