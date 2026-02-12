package com.quarkdown.quarkdoc.dokka.transformers.optional

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.factory.ValueFactory.enum
import com.quarkdown.quarkdoc.dokka.kdoc.buildDocTags
import com.quarkdown.quarkdoc.dokka.page.WIKI_ROOT
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocParameterDocumentationTransformer
import com.quarkdown.quarkdoc.dokka.util.hasAnnotation
import com.quarkdown.quarkdoc.dokka.util.scrapingAnchor
import com.quarkdown.quarkdoc.reader.anchors.Anchors
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DefaultValue
import org.jetbrains.dokka.model.doc.Dl
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.plugability.DokkaContext

private const val BODY_PARAMETER_WIKI_URL = WIKI_ROOT + "syntax-of-a-function-call#block-vs-inline-function-calls"

private const val NAMED_PARAMETER_WIKI_URL = WIKI_ROOT + "syntax-of-a-function-call"

/**
 * Transformer that appends documentation to parameters
 * indicating additional properties, such as:
 * - whether the parameter is optional
 */
class AdditionalParameterPropertiesTransformer(
    context: DokkaContext,
) : QuarkdocParameterDocumentationTransformer<AdditionalParameterPropertiesTransformer.ParameterProperties>(context) {
    /**
     * Additional properties of a parameter that can be documented.
     * @property isOptional whether the parameter is optional
     * @property isLikelyNamed whether the parameter is likely passed as a named argument
     * @property isLikelyBody whether the parameter is likely passed as a body argument
     */
    data class ParameterProperties(
        val isOptional: Boolean,
        val isLikelyNamed: Boolean,
        val isLikelyBody: Boolean,
    )

    override fun extractValue(parameter: DParameter) =
        ParameterProperties(
            isOptional = parameter.extra[DefaultValue] != null,
            isLikelyNamed = parameter.hasAnnotation<LikelyNamed>() || parameter.hasAnnotation<Name>(),
            isLikelyBody = parameter.hasAnnotation<LikelyBody>(),
        )

    /**
     * @return the documentation content to add to the parameter documentation,
     * which lists the enum entries of the given [enum].
     */
    override fun createNewDocumentation(value: ParameterProperties): List<DocTag> =
        buildDocTags {
            if (value.isLikelyBody) {
                listItem {
                    scrapingAnchor(Anchors.LIKELY_BODY)
                    text("Likely a ")
                    link(address = BODY_PARAMETER_WIKI_URL, "body argument")
                }
            }
            if (value.isOptional) {
                listItem {
                    scrapingAnchor(Anchors.OPTIONAL)
                    text("Optional")
                }
            }
            if (value.isLikelyNamed) {
                listItem {
                    scrapingAnchor(Anchors.LIKELY_NAMED)
                    text("Likely ")
                    link(address = NAMED_PARAMETER_WIKI_URL, "named")
                }
            }
        }.takeUnless { it.isEmpty() }
            ?.let {
                listOf(
                    Dl(listOf(Ul(it))),
                )
            } ?: emptyList()

    override fun mergeDocumentationContent(
        old: List<DocTag>,
        new: List<DocTag>,
    ) = new + old
}
