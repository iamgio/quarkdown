package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.plugability.DokkaContext

private const val VALUE_SUFFIX = "Value"

/**
 * Transformer that renames [com.quarkdown.core.function.value.Value] and subclasses in the signature to a more human-readable form.
 * For example:
 * - `NumberValue` -> `Number`
 * - `IterableValue` -> `Iterable`
 * - `OutputValue` -> `Any`
 */
class ValueTypeTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformType(type: GenericTypeConstructor): AnyWithChanges<GenericTypeConstructor> {
        val dri = type.dri
        val className = dri.classNames ?: return type.unchanged()

        val newClassName: String =
            when {
                // className == OutputValue::class.simpleName -> "Any"
                className.endsWith(VALUE_SUFFIX) -> className.removeSuffix(VALUE_SUFFIX)
                else -> return type.unchanged()
            }

        return type
            .copy(dri = dri.copy(classNames = newClassName))
            .changed()
    }
}
