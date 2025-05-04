package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.quarkdoc.dokka.kdoc.DokkaDocumentation
import com.quarkdown.quarkdoc.dokka.kdoc.mapDocumentation
import com.quarkdown.quarkdoc.dokka.storage.RenamingsStorage
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.See
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that renames functions and parameters annotated with `@Name` within the KDoc documentation.
 * @see DocumentableNameTransformer for signature-side renaming
 * @see RenamingsStorage
 */
class DocumentationNameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val documentation =
            updateDocumentationReferences(documentation = function.documentation, parameters = function.parameters)

        return function
            .copy(documentation = documentation)
            .changed(documentation != function.documentation)
    }

    override fun transformParameter(parameter: DParameter): AnyWithChanges<DParameter> {
        val documentation =
            updateDocumentationReferences(
                parameter.documentation,
                listOf(parameter),
            )

        return parameter
            .copy(documentation = documentation)
            .changed(documentation != parameter.documentation)
    }

    /**
     * Updates the parameter names in the documentation of a function or parameter.
     * @param documentation the documentation to update
     * @param parameters the list of parameters to update in `@param` tags
     */
    private fun updateDocumentationReferences(
        documentation: DokkaDocumentation,
        parameters: List<DParameter>,
    ) = mapDocumentation(documentation) {
        // [oldName] -> [newName]
        register(DocumentationLink::class) { link ->
            val renaming = RenamingsStorage[link.dri] ?: return@register link

            val text = link.children.singleOrNull() as? Text ?: return@register link
            val newText = text.copy(body = renaming.newName)
            val newParams = link.params.toMutableMap().apply { this["href"] = "[${renaming.newName}]" }

            link.copy(children = listOf(newText), params = newParams)
        }

        // @param oldName -> @param newName
        register(Param::class) { param ->
            val address = parameters.find { it.name == param.name }?.dri ?: return@register param
            val renaming = RenamingsStorage[address] ?: return@register param

            param.copy(name = renaming.newName)
        }

        // @see oldName -> @see newName
        register(See::class) { see ->
            val renaming = RenamingsStorage[requireNotNull(see.address)] ?: return@register see
            see.copy(name = renaming.newName)
        }
    }
}
