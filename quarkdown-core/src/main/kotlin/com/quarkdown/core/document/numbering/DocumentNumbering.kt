package com.quarkdown.core.document.numbering

import com.quarkdown.amber.annotations.Mergeable
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.inline.ReferenceFootnote

/**
 * An immutable group of [NumberingFormat]s for different types of elements ([Node]s) in a document.
 * @param headings format for [Heading]s
 * @param figures format for [Figure]s
 * @param tables format for [Table]s
 * @param footnotes format for [FootnoteDefinition] and [ReferenceFootnote]s
 * @param extra extra, dynamic formats for custom elements (e.g. [com.quarkdown.core.ast.quarkdown.block.Numbered])
 */
@Mergeable
data class DocumentNumbering(
    val headings: NumberingFormat? = null,
    val figures: NumberingFormat? = null,
    val tables: NumberingFormat? = null,
    val footnotes: NumberingFormat? = null,
    val extra: Map<String, NumberingFormat> = emptyMap(),
)
