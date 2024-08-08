package eu.iamgio.quarkdown.rendering.tag

import eu.iamgio.quarkdown.ast.base.inline.CriticalContent
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.rendering.NodeRenderer

/**
 * A converter of [eu.iamgio.quarkdown.ast.Node]s into tag-based output code,
 * by using a DSL-like approach provided by [TagBuilder].
 * @param context rendering context
 */
abstract class TagNodeRenderer<B : TagBuilder>(val context: Context) : NodeRenderer {
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
