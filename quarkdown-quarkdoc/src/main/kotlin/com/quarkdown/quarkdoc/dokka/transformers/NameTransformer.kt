package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.util.filterNotNullEntries
import com.quarkdown.core.util.trimDelimiters
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
 * Old-new parameter name pairs.
 */
private typealias ParameterRenamings = Map<String, String>

/**
 * Transformer that renames functions and parameters annotated with `@Name` in the generated documentation.
 */
class NameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        println("Y ${function.name}")

        // Parameters annotated with `@Name` are renamed.
        val parameters = function.parameters.map(::overrideNameIfAnnotated).map { it.target!! }

        // Old-new parameter name pairs.
        val parameterRenamings: ParameterRenamings =
            function.parameters
                .asSequence()
                .mapIndexed { index, parameter -> parameter.name to parameters[index].name }
                .filterNotNullEntries()
                .toMap()

        // Parameter documentation must be updated to reflect the new names.
        val documentation =
            updateDocumentationReferences(
                parameterRenamings = parameterRenamings,
                documentation = function.documentation,
            )

        // The function name is updated if it is annotated with `@Name`.
        return overrideNameIfAnnotated(function).merge {
            it
                .copy(parameters = parameters, documentation = documentation)
                .changed(changed = parameterRenamings.isNotEmpty())
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
                parameterRenamings = mapOf(parameter.name!! to newName),
                parameter.documentation,
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
        parameterRenamings: ParameterRenamings,
        documentation: DokkaDocumentation,
    ) = mapDocumentation(documentation) {
        // @param oldName -> @param newName
        register(Param::class) { param ->
            parameterRenamings[param.name]
                ?.let { param.copy(name = it) }
                ?: param
        }

        // [oldName] -> [newName]
        register(DocumentationLink::class) { link ->
            val oldName = link.params["href"]?.trimDelimiters()
            val referencedParameter = parameterRenamings[oldName] ?: return@register link

            val text = link.children.singleOrNull() as? Text ?: return@register link
            val newText = text.copy(body = referencedParameter)
            val newParams = link.params.toMutableMap().apply { this["href"] = "[$referencedParameter]" }

            link.copy(children = listOf(newText), params = newParams)
        }

        // @see oldName -> @see newName
        register(See::class) { see ->
            val newName = RenamingsStorage.functionRenamings[see.address] ?: return@register see
            see.copy(name = newName)
        }
    }
}
