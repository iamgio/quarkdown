package com.quarkdown.quarkdoc.dokka.transformers.optional

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.value.factory.ValueFactory.enum
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocParameterDocumentationTransformer
import com.quarkdown.quarkdoc.dokka.util.hasAnnotation
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DefaultValue
import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.Dl
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.plugability.DokkaContext

private const val BODY_PARAMETER_WIKI_URL =
    "https://github.com/iamgio/quarkdown/wiki/syntax-of-a-function-call#block-vs-inline-function-calls"

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
     * @property isBody whether the parameter is likely passed as a body argument
     */
    data class ParameterProperties(
        val isOptional: Boolean,
        val isBody: Boolean,
    )

    override fun extractValue(parameter: DParameter) =
        ParameterProperties(
            isOptional = parameter.extra[DefaultValue] != null,
            isBody = parameter.hasAnnotation<LikelyBody>(),
        )

    /**
     * @return the documentation content to add to the parameter documentation,
     * which lists the enum entries of the given [enum].
     */
    override fun createNewDocumentation(value: ParameterProperties): List<DocTag> =
        buildList {
            if (value.isBody) {
                this +=
                    Li(
                        listOf(
                            Text("Likely passed as a "),
                            A(
                                listOf(
                                    Text("body argument"),
                                ),
                                params = mapOf("href" to BODY_PARAMETER_WIKI_URL),
                            ),
                        ),
                    )
            }
            if (value.isOptional) {
                this +=
                    Li(
                        listOf(
                            Text("Optional"),
                        ),
                    )
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
