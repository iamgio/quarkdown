package com.quarkdown.core.ast.quarkdown.bibliography

import com.quarkdown.core.ast.Node
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.citation.ResolvedBibliographyEntryProperty
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Represents a citation to a bibliography entry.
 * @param citationKey the key used to identify the bibliography entry
 * @see getEntry
 */
class BibliographyCitation(
    val citationKey: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

/**
 * A bibliography entry paired with the styled bibliography node it belongs to.
 * @see ResolvedBibliographyEntryProperty
 */
typealias ResolvedBibliographyEntry = Pair<BibliographyEntry, BibliographyView>

/**
 * @param context context where bibliography data is stored
 * @return the bibliography entry associated with this node within the document handled by [context],
 * or `null` if the entry for [this] node is not registered or resolved
 */
fun BibliographyCitation.getEntry(context: Context): ResolvedBibliographyEntry? =
    context.attributes.of(this)[ResolvedBibliographyEntryProperty]

/**
 * Registers the given [entry] as the bibliography entry associated with this citation node within the document handled by [context].
 * @param context context where bibliography data is stored
 * @param entry the bibliography entry to associate with this node
 * @see com.quarkdown.core.context.hooks.bibliography.BibliographyCitationHook
 */
fun BibliographyCitation.setEntry(
    context: MutableContext,
    entry: ResolvedBibliographyEntry,
) {
    context.attributes.of(this) += ResolvedBibliographyEntryProperty(entry)
}
