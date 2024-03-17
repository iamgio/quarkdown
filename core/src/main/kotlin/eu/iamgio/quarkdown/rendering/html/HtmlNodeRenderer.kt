package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.rendering.NodeVisitor

/**
 *
 */
class HtmlNodeRenderer : NodeVisitor<CharSequence> {
    override fun visit(node: AstRoot): CharSequence =
        "<!DOCTYPE html>\n" +
            tag(
                "html",
                tag(
                    "head",
                    tag("meta")
                        .attribute("charset", "UTF-8")
                        .empty(true),
                ),
                tag("body") {
                    node.children
                },
            ).build()

    override fun visit(node: PlainText): CharSequence {
        return node.text
    }

    override fun visit(node: Strong): CharSequence =
        tag("strong") {
            node.children
        }.build()
}
