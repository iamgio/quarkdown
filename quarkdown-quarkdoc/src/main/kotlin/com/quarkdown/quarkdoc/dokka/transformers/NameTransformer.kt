package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.quarkdoc.dokka.kdoc.DokkaDocumentation
import com.quarkdown.quarkdoc.dokka.kdoc.mapDocumentation
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.See
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that renames functions and parameters annotated with `@Name` in the generated documentation.
 */
class NameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        // Parameters annotated with `@Name` are renamed.
        val parameters = function.parameters.map(::overrideNameIfAnnotated).map { it.target!! }

        // Parameter documentation must be updated to reflect the new names.
        val documentation = // todo refactor: one extension for documentation and one for names
            updateDocumentationReferences(documentation = function.documentation, parameters = function.parameters)

        // The function name is updated if it is annotated with `@Name`.
        return overrideNameIfAnnotated(function).merge {
            it
                .copy(parameters = parameters, documentation = documentation)
                .changed(changed = true) // parameterRenamings.isNotEmpty())
        }
    }

    private fun <D : Documentable> overrideNameIfAnnotated(documentable: D): AnyWithChanges<D> {
        val newName = getOverriddenName(documentable) ?: return documentable.unchanged()

        @Suppress("UNCHECKED_CAST")
        return when (documentable) {
            is DFunction -> updateFunctionName(documentable, newName)
            is DParameter -> updateParameterName(documentable, newName)
            else -> null
        }?.let { it as D }
            ?.changed()
            ?: documentable.unchanged()
    }

    private fun updateFunctionName(
        function: DFunction,
        newName: String,
    ): DFunction = function.copy(name = newName)

    private fun updateParameterName(
        parameter: DParameter,
        newName: String,
    ): DParameter {
        assert(parameter.name != null)

        // Renaming must also be reflected in the parameter documentation.
        val documentation =
            updateDocumentationReferences(
                // parameterRenamings = mapOf(parameter.name!! to newName),
                parameter.documentation,
                listOf(parameter),
            )

        return parameter.copy(
            name = newName,
            documentation = documentation,
        )
    }

    /**
     * Updates the parameter names in the documentation of a function or parameter.
     * @param parameterRenamings old-new parameter name pairs
     * @param documentation the documentation to update
     */
    private fun updateDocumentationReferences(
        documentation: DokkaDocumentation,
        parameters: List<DParameter>,
    ) = mapDocumentation(documentation) {
        // @param oldName -> @param newName
        register(Param::class) { param ->
            val address = parameters.find { it.name == param.name }?.dri ?: return@register param
            val renaming = RenamingsStorage[address] ?: return@register param

            param.copy(name = renaming.newName)
        }

        // [oldName] -> [newName]
        register(DocumentationLink::class) { link ->
            val renaming = RenamingsStorage[link.dri] ?: return@register link

            val text = link.children.singleOrNull() as? Text ?: return@register link
            val newText = text.copy(body = renaming.newName)
            val newParams = link.params.toMutableMap().apply { this["href"] = "[${renaming.newName}]" }

            link.copy(children = listOf(newText), params = newParams)
        }

        // @see oldName -> @see newName
        register(See::class) { see ->
            val renaming = RenamingsStorage[requireNotNull(see.address)] ?: return@register see
            see.copy(name = renaming.newName)
        }
    }
}
