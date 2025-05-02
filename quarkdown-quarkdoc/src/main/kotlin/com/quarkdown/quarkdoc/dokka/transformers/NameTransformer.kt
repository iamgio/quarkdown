package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.quarkdoc.dokka.util.extractAnnotation
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that renames functions and parameters annotated with `@Name` in the generated documentation.
 * @param context the Dokka context
 */
class NameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction) = overrideNameIfAnnotated(function)

    override fun transformParameter(parameter: DParameter) = overrideNameIfAnnotated(parameter)

    private fun <D : Documentable> overrideNameIfAnnotated(documentable: D): AnyWithChanges<D> {
        val nameAnnotation = documentable.extractAnnotation<Name>()
        val newName = nameAnnotation?.params["name"]?.toString() ?: return AnyWithChanges(documentable, changed = false)

        @Suppress("UNCHECKED_CAST")
        return when (documentable) {
            is DFunction -> documentable.copy(name = newName)
            is DParameter -> documentable.copy(name = newName)
            else -> null
        }?.let { it as D }
            ?.let { AnyWithChanges(it, changed = true) }
            ?: AnyWithChanges(documentable, changed = false)
    }
}
