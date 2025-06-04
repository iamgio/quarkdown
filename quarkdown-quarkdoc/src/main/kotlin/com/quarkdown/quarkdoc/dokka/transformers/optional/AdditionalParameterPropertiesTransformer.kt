package com.quarkdown.quarkdoc.dokka.transformers.optional

import com.quarkdown.core.function.value.factory.ValueFactory.enum
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocParameterDocumentationTransformer
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DefaultValue
import org.jetbrains.dokka.model.doc.Dl
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that appends documentation to parameters
 * indicating additional properties, such as:
 * - whether the parameter is optional
 */
class AdditionalParameterPropertiesTransformer(
    context: DokkaContext,
) : QuarkdocParameterDocumentationTransformer<AdditionalParameterPropertiesTransformer.ParameterProperties>(context) {
    data class ParameterProperties(
        val isOptional: Boolean,
    )

    override fun extractValue(parameter: DParameter) =
        ParameterProperties(
            isOptional = parameter.extra[DefaultValue] != null,
        )

    /**
     * @return the documentation content to add to the parameter documentation,
     * which lists the enum entries of the given [enum].
     */
    override fun createNewDocumentation(value: ParameterProperties): List<DocTag> =
        buildList {
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
