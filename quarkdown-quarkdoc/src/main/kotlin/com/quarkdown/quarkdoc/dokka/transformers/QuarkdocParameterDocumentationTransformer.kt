package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.quarkdoc.dokka.kdoc.mapDocumentation
import com.quarkdown.quarkdoc.dokka.util.tryCopy
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that extends the documentation of function parameters,
 * according to additional information extracted from the parameters, of type [T].
 * @param T the type of the additional information extracted from the parameters
 * @see com.quarkdown.quarkdoc.dokka.transformers.optional.AdditionalParameterPropertiesTransformer
 * @see com.quarkdown.quarkdoc.dokka.transformers.enumeration.EnumParameterEntryListerTransformer
 */
abstract class QuarkdocParameterDocumentationTransformer<T>(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    /**
     * @return the value of type [T] extracted from the given [parameter], if any
     */
    protected abstract fun extractValue(parameter: DParameter): T?

    /**
     * @return the parameters, among [parameters], that have a non-null extracted value, associated with their enum declaration.
     */
    private fun associateParameters(parameters: List<DParameter>): Map<String, T> =
        parameters
            .asSequence()
            .map { it.name to extractValue(it) }
            .filterNotNullEntries()
            .toMap()

    /**
     * @return the documentation content to add to the parameter documentation
     */
    protected abstract fun createNewDocumentation(value: T): List<DocTag>

    /**
     * Merges the old and new documentation content.
     * For example, `old + new` to append the new content to the old one.
     * @param old the existing documentation content
     * @param new the new documentation content to add, form [createNewDocumentation]
     * @return the merged documentation content
     */
    protected abstract fun mergeDocumentationContent(
        old: List<DocTag>,
        new: List<DocTag>,
    ): List<DocTag>

    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val values: Map<String, T> =
            associateParameters(function.parameters)
                .takeIf { it.isNotEmpty() }
                ?: return function.unchanged()

        // Updates the documentation of the parameters to include the properties.
        val documentation =
            mapDocumentation(function.documentation) {
                register(Param::class) { param ->
                    val value = values[param.name] ?: return@register param
                    val root = param.root
                    val documentation = mergeDocumentationContent(root.children, createNewDocumentation(value))
                    param.copy(
                        root = root.tryCopy(newChildren = documentation),
                    )
                }
            }

        return function.copy(documentation = documentation).changed()
    }
}
