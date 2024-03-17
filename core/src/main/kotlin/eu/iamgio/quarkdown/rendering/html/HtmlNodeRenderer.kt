package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.rendering.NodeVisitor

/**
 *
 */
class HtmlNodeRenderer : NodeVisitor<CharSequence> {
    override fun visit(node: PlainText): CharSequence {
        return node.text
    }
}
