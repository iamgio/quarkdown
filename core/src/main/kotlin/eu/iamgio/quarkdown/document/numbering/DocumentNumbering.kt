package eu.iamgio.quarkdown.document.numbering

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.Table

/**
 * An immutable group of [NumberingFormat]s for different types of elements ([Node]s) in a document.
 * @param headings format for [Heading]s
 * @param figures format for [Figure]s
 * @param tables format for [Table]s
 * @param extra extra, dynamic formats for custom elements (e.g. [eu.iamgio.quarkdown.ast.quarkdown.block.Numbered])
 */
data class DocumentNumbering(
    val headings: NumberingFormat? = null,
    val figures: NumberingFormat? = null,
    val tables: NumberingFormat? = null,
    val extra: Map<String, NumberingFormat> = emptyMap(),
)
