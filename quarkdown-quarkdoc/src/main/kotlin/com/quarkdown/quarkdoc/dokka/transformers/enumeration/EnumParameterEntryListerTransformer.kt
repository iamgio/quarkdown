package com.quarkdown.quarkdoc.dokka.transformers.enumeration

import com.quarkdown.core.function.toQuarkdownNamingFormat
import com.quarkdown.quarkdoc.dokka.kdoc.buildDocTags
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocParameterDocumentationTransformer
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.adapters.QuarkdocEnumAdapters
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * A transformer that, given a parameter that expects an enum value,
 * lists the enum entries in its documentation.
 */
class EnumParameterEntryListerTransformer(
    context: DokkaContext,
) : QuarkdocParameterDocumentationTransformer<QuarkdocEnum>(context) {
    /**
     * @return the enum type of the parameter, if it is an enum
     */
    override fun extractValue(parameter: DParameter): QuarkdocEnum? = parameter.type.driOrNull?.let(QuarkdocEnumAdapters::fromDRI)

    override fun createNewDocumentation(value: QuarkdocEnum): List<DocTag> =
        buildDocTags {
            h4 { text("Values") }
            unorderedList {
                value.entries.forEach { entry ->
                    listItem {
                        link(dri = entry.dri) {
                            codeInline(entry.name.toQuarkdownNamingFormat())
                        }
                    }
                }
            }
        }

    override fun mergeDocumentationContent(
        old: List<DocTag>,
        new: List<DocTag>,
    ) = old + new
}
