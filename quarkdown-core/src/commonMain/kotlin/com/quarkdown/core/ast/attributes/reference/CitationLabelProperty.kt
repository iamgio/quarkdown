package com.quarkdown.core.ast.attributes.reference

import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.property.Property

/**
 * Pre-computed citation label for a [BibliographyCitation] node.
 *
 * Labels are computed eagerly in document order during the
 * [com.quarkdown.core.context.hooks.reference.BibliographyCitationResolverHook] phase,
 * so that parallel rendering can read them without depending on visit order.
 * @param value the formatted citation label (e.g. `"[1]"`, `"[1, 2]"`)
 */
data class CitationLabelProperty(
    override val value: String,
) : Property<String> {
    companion object : Property.Key<String>

    override val key = CitationLabelProperty
}

/**
 * Retrieves the pre-computed citation label for this node.
 * @param context context where the property is stored
 * @return the citation label, or `null` if not pre-computed
 */
fun BibliographyCitation.getCitationLabel(context: Context): String? = context.attributes.of(this)[CitationLabelProperty]

/**
 * Stores a pre-computed citation label on this node.
 * @param context context where the property is stored
 * @param label the citation label to store
 */
fun BibliographyCitation.setCitationLabel(
    context: MutableContext,
    label: String,
) {
    context.attributes.of(this) += CitationLabelProperty(label)
}
