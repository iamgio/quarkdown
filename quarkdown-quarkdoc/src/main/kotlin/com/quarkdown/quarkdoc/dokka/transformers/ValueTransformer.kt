package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.plugability.DokkaContext

private const val VALUE_SUFFIX = "Value"

/**
 * Transformer that renames [com.quarkdown.core.function.value.Value] and subclasses in the signature to a more human-readable form.
 * For example:
 * - `NumberValue` -> `Number`
 * - `IterableValue` -> `Iterable`
 */
class ValueTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val type = function.type as? GenericTypeConstructor ?: return function.unchanged()
        val dri = type.dri
        val className = dri.classNames ?: return function.unchanged()

        val newClassName: String =
            when {
                className.endsWith(VALUE_SUFFIX) -> className.removeSuffix(VALUE_SUFFIX)
                else -> return function.unchanged()
            }

        return function
            .copy(
                type =
                    type.copy(
                        dri = dri.copy(classNames = newClassName),
                    ),
            ).changed()
    }
}
