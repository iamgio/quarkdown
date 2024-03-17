package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.rendering.NodeVisitor

/**
 * A renderer for [eu.iamgio.quarkdown.ast.Node]s that export their content into valid HTML code.
 */
class HtmlNodeRenderer : NodeVisitor<CharSequence> {
    override fun visit(node: AstRoot): CharSequence =
        "<!DOCTYPE html>\n" +
            buildTag("html") {
                tag("head") {
                    tag("meta")
                        .attribute("charset", "UTF-8")
                        .void(true)
                }
                tag("body") {
                    +node.children
                }
            }

    // Block

    // Inline

    override fun visit(node: PlainText): CharSequence {
        return node.text
    }

    override fun visit(node: Emphasis): CharSequence = buildTag("em", node.children)

    override fun visit(node: Strong): CharSequence = buildTag("strong", node.children)

    override fun visit(node: StrongEmphasis): CharSequence =
        buildTag("em") {
            tag("strong") {
                +node.children
            }
        }

    override fun visit(node: Strikethrough): CharSequence = buildTag("del", node.children)
}
