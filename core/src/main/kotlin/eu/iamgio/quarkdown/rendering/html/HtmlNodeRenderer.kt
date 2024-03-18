package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.ast.resolveLinkReference
import eu.iamgio.quarkdown.rendering.NodeVisitor
import eu.iamgio.quarkdown.util.toPlainText

/**
 * A renderer for [eu.iamgio.quarkdown.ast.Node]s that export their content into valid HTML code.
 * @param attributes additional attributes of the node tree
 */
class HtmlNodeRenderer(private val attributes: AstAttributes) : NodeVisitor<CharSequence> {
    override fun visit(node: AstRoot) =
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

    override fun visit(node: Newline) = ""

    override fun visit(node: LinkDefinition) = "" // Not rendered

    override fun visit(node: Paragraph) = buildTag("p", node.text)

    // Inline

    override fun visit(node: Comment) = "" // Ignored

    override fun visit(node: LineBreak) =
        tagBuilder("br")
            .void(true)
            .build()

    override fun visit(node: CriticalContent) =
        when (node.text) {
            "&" -> "&amp;"
            "<" -> "&lt;"
            ">" -> "&gt;"
            "\"" -> "&quot;"
            "\'" -> "&#39;"
            else -> node.text
        }

    override fun visit(node: Link) =
        tagBuilder("a") {
            +node.label
        }
            .attribute("href", node.url)
            .optionalAttribute("title", node.title)
            .build()

    override fun visit(node: ReferenceLink) =
        // The fallback node is rendered if a corresponding definition can't be found.
        (attributes.resolveLinkReference(node) ?: node.fallback())
            .accept(this)

    override fun visit(node: Image) =
        tagBuilder("img")
            .attribute("src", node.link.url)
            .attribute("alt", node.link.label.toPlainText(renderer = this)) // Emphasis is discarded (CommonMark 6.4)
            .optionalAttribute("title", node.link.title)
            .void(true)
            .build()

    override fun visit(node: ReferenceImage): CharSequence {
        TODO("Not yet implemented")
    }

    override fun visit(node: Text) = node.text

    override fun visit(node: CodeSpan) = buildTag("code", node.text)

    override fun visit(node: Emphasis) = buildTag("em", node.children)

    override fun visit(node: Strong) = buildTag("strong", node.children)

    override fun visit(node: StrongEmphasis) =
        buildTag("em") {
            tag("strong") {
                +node.children
            }
        }

    override fun visit(node: Strikethrough) = buildTag("del", node.children)
}
