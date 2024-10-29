package eu.iamgio.quarkdown.document.numbering

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure

/**
 * An immutable group of [NumberingFormat]s for different types of elements ([Node]s) in a document.
 * @param headings format for [Heading]s
 * @param figures format for [ImageFigure]s
 */
data class DocumentNumbering(
    val headings: NumberingFormat? = null,
    val figures: NumberingFormat? = null,
) {
    /**
     * @return the format to apply to the given [node] based on its type,
     * or `null` if the given node type does not expect a numbering format
     */
    fun getFormatForNode(node: Node): NumberingFormat? =
        when (node) {
            is Heading -> headings
            is ImageFigure -> figures
            else -> null
        }
}
