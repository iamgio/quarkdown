package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A converter of [eu.iamgio.quarkdown.ast.Node]s into tag-based output code,
 * by using a DSL-like approach provided by [TagBuilder].
 */
abstract class NodeRenderer<B : TagBuilder> : NodeVisitor<CharSequence> {
    /**
     * Factory method that creates a new builder.
     * @param name name of the tag to open
     * @param pretty whether the output code should be pretty
     */
    abstract fun createBuilder(
        name: String,
        pretty: Boolean,
    ): B

    /**
     * @param unescaped input to escape critical content for
     * @return the input string with the critical content escaped into safe content
     *         (e.g. in HTML `<` is escaped to `&lt;`).
     * @see CriticalContent
     */
    abstract fun escapeCriticalContent(unescaped: String): CharSequence

    /**
     * @see escapeCriticalContent
     */
    override fun visit(node: CriticalContent) = escapeCriticalContent(node.text)
}
