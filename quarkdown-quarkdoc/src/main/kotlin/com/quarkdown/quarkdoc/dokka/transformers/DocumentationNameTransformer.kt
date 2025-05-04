package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.quarkdoc.dokka.kdoc.DocumentationReferencesTransformer
import com.quarkdown.quarkdoc.dokka.kdoc.DokkaDocumentation
import com.quarkdown.quarkdoc.dokka.storage.RenamingsStorage
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
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
    private fun <D : Documentable> applyDocumentation(
        documentable: D,
        parameters: List<DParameter>,
        copy: (DokkaDocumentation) -> D,
    ): AnyWithChanges<D> {
        val documentation =
            NameTransformerDocumentationReferencesTransformer().transformReferences(
                documentation = documentable.documentation,
                parameters = parameters,
            )
        return copy(documentation).changed(documentation != documentable.documentation)
    }

    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> =
        applyDocumentation(function, function.parameters) {
            function.copy(documentation = it)
        }

    override fun transformParameter(parameter: DParameter): AnyWithChanges<DParameter> =
        applyDocumentation(parameter, listOf(parameter)) {
            parameter.copy(documentation = it)
        }
}

/**
 * Transformer that applies renamings to KDoc references in the documentation.
 */
private class NameTransformerDocumentationReferencesTransformer : DocumentationReferencesTransformer {
    override fun onLink(link: DocumentationLink): DocumentationLink {
        val renaming = RenamingsStorage[link.dri] ?: return link
        val textChild = link.children.singleOrNull() as? Text ?: return link
        val newParams = link.params.toMutableMap().apply { this["href"] = "[${renaming.newName}]" }
        return link.copy(
            children = listOf(textChild.copy(body = renaming.newName)),
            params = newParams,
        )
    }

    override fun onParam(
        param: Param,
        actualParameter: DParameter,
    ): Param {
        val renaming = RenamingsStorage[actualParameter.dri] ?: return param
        return param.copy(name = renaming.newName)
    }

    override fun onSee(see: See): See {
        val renaming = RenamingsStorage[requireNotNull(see.address)] ?: return see
        return see.copy(name = renaming.newName)
    }
}
