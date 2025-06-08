package com.quarkdown.quarkdoc.dokka.transformers.optional

import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.reflect.annotation.Name
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

private const val NAMED_PARAMETER_WIKI_URL =
    "https://github.com/iamgio/quarkdown/wiki/syntax-of-a-function-call"

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
        buildList {
            if (value.isLikelyBody) {
                this +=
                    Li(
                        listOf(
                            Text("Likely a "),
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
            if (value.isLikelyNamed) {
                this +=
                    Li(
                        listOf(
                            Text("Likely "),
                            A(
                                listOf(
                                    Text("named"),
                                ),
                                params = mapOf("href" to NAMED_PARAMETER_WIKI_URL),
                            ),
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
