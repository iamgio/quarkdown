package eu.iamgio.quarkdown.document.numbering

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure

/**
 * An immutable group of [NumberingFormat]s for different types of elements ([Node]s) in a document.
 * @param headings format for [Heading]s
 * @param figures format for [ImageFigure]s
 * @param tables format for [Table]s
 */
data class DocumentNumbering(
    val headings: NumberingFormat? = null,
    val figures: NumberingFormat? = null,
    val tables: NumberingFormat? = null,
)
