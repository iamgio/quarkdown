package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.id.Identifiable
import com.quarkdown.core.ast.attributes.id.IdentifierProvider
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.numbering.DecimalNumberingSymbol
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.property.Property
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creation of a footnote definition, referenceable by a [com.quarkdown.core.ast.base.inline.ReferenceFootnote].
 * @param label inline content of the referenceable label, which should match that of the [com.quarkdown.core.ast.base.inline.ReferenceFootnote]s
 * @param text inline content of the footnote
 * @param index index of the footnote in the document, in order of reference, or `null` if not linked to any reference
 */
class FootnoteDefinition(
    val label: String,
    override val text: InlineContent,
) : TextNode,
    Identifiable {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    override fun <T> accept(visitor: IdentifierProvider<T>) = visitor.visit(this)
}

/**
 * Property that stores the index of a [FootnoteDefinition] within the document.
 * The index associates the order of the footnote in the document according to the references to it.
 */
private data class FootnoteIndexProperty(
    override val value: Int,
) : Property<Int> {
    companion object : Property.Key<Int>

    override val key = FootnoteIndexProperty
}

/**
 * @param context context where footnote data is stored
 * @return the index of this footnote definition in the document, or `null` if it is not linked to any reference
 */
fun FootnoteDefinition.getIndex(context: Context): Int? = context.attributes.of(this)[FootnoteIndexProperty]

/**
 * Registers the footnote index of this node within the document handled by [context],
 * according to the order of references to it.
 * @param context context where footnote data is stored
 * @param index index of the footnote definition in the document, in order of reference
 */
fun FootnoteDefinition.setIndex(
    context: MutableContext,
    index: Int,
) {
    context.attributes.of(this) += FootnoteIndexProperty(index)
}

/**
 * Formats the index of this footnote definition according to the numbering format defined in the document,
 * or a default numbering format if none is defined. The default format is `1, 2, 3, ...` (decimal numbering).
 * @param context context where footnote data is stored
 * @return formatted index of the footnote definition, or `null` if it is not linked to any reference
 * @see getIndex
 */
fun FootnoteDefinition.getFormattedIndex(context: Context): String? {
    val index = getIndex(context) ?: return null
    val format =
        context.documentInfo.numberingOrDefault?.footnotes
            ?: NumberingFormat(DecimalNumberingSymbol)

    return format.format(index + 1)
}
