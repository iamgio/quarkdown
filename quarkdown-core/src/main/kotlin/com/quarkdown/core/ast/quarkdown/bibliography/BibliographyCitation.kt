package com.quarkdown.core.ast.quarkdown.bibliography

import com.quarkdown.core.ast.attributes.reference.ReferenceNode
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Represents a citation to a bibliography entry.
 * @param citationKey the key used to identify the bibliography entry
 */
class BibliographyCitation(
    val citationKey: String,
) : ReferenceNode<BibliographyCitation, Pair<BibliographyEntry, BibliographyView>> {
    override val reference: BibliographyCitation = this

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
