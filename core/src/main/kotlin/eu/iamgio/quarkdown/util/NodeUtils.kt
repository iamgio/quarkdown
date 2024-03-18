package eu.iamgio.quarkdown.util

import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.PlainTextNode
import eu.iamgio.quarkdown.rendering.NodeVisitor

/**
 * Converts processed [InlineContent] to its plain text representation.
 * For example, the Markdown input `foo **bar `baz`**` has `foo bar baz` as its plain text.
 * @param renderer optional renderer to use to render critical content
 * @return plain text of the inline content
 * @see PlainTextNode
 */
fun InlineContent.toPlainText(renderer: NodeVisitor<CharSequence>? = null): String {
    val builder = StringBuilder()

    // Recursive DFS visit.
    fun visit(content: InlineContent) {
        content.forEach {
            when {
                it is CriticalContent && renderer != null -> builder.append(renderer.visit(it))
                it is PlainTextNode -> builder.append(it.text)
                it is NestableNode -> visit(it.children)
            }
        }
    }

    visit(this)

    return builder.toString()
}
