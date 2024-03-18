package eu.iamgio.quarkdown.util

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.PlainTextNode

/**
 * Converts processed [InlineContent] to its plain text representation.
 * For example, the Markdown input `foo **bar `baz`**` has `foo bar baz` as its plain text.
 * @return plain text of the inline content
 * @see PlainTextNode
 */
fun InlineContent.toPlainText(): String {
    val builder = StringBuilder()

    // Recursive DFS visit.
    fun visit(content: InlineContent) {
        content.forEach {
            when (it) {
                is PlainTextNode -> builder.append(it.text)
                is NestableNode -> visit(it.children)
            }
        }
    }

    visit(this)

    return builder.toString()
}
