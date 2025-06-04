package com.quarkdown.quarkdoc.dokka.transformers.optional

import com.quarkdown.core.function.value.factory.ValueFactory.enum
import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.quarkdoc.dokka.kdoc.mapDocumentation
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.tryCopy
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DefaultValue
import org.jetbrains.dokka.model.doc.Dl
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Param
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
) : QuarkdocDocumentableReplacerTransformer(context) {
    private data class ParameterProperties(
        val isOptional: Boolean,
    )

    private fun extractProperties(parameter: DParameter) =
        ParameterProperties(
            isOptional = parameter.extra[DefaultValue] != null,
        )

    /**
     * @return the parameters, among [parameters], that expect an enum value, associated with their enum declaration.
     */
    private fun associateParameters(parameters: List<DParameter>): Map<String, ParameterProperties> =
        parameters
            .asSequence()
            .map { it.name to extractProperties(it) }
            .filterNotNullEntries()
            .toMap()

    /**
     * @return the documentation content to add to the parameter documentation,
     * which lists the enum entries of the given [enum].
     */
    private fun createNewDocumentationContent(properties: ParameterProperties): List<DocTag> =
        buildList {
            if (properties.isOptional) {
                this +=
                    Li(
                        listOf(
                            Text("Optional"),
                        ),
                    )
            }
        }.let {
            listOf(
                Dl(listOf(Ul(it))),
            )
        }

    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val properties: Map<String, ParameterProperties> =
            associateParameters(function.parameters)
                .takeIf { it.isNotEmpty() }
                ?: return function.unchanged()

        // Updates the documentation of the parameters to include the properties.
        val documentation =
            mapDocumentation(function.documentation) {
                register(Param::class) { param ->
                    val enum = properties[param.name] ?: return@register param
                    val root = param.root
                    param.copy(
                        root = root.tryCopy(newChildren = createNewDocumentationContent(enum) + root.children),
                    )
                }
            }

        return function.copy(documentation = documentation).changed()
    }
}
