package com.quarkdown.quarkdoc.dokka.transformers.type

import com.quarkdown.core.function.value.InputValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.driOrNull
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.Variance
import org.jetbrains.dokka.plugability.DokkaContext

private const val VALUE_SUFFIX = "Value"

/**
 * Transformer that renames [Value] and subclasses in the signature to a more human-readable form.
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

        val (newClassName: String, newProjections: List<Projection>?) =
            when {
                // Value<*>, InputValue<*>, OutputValue<*> -> Any
                className == Value::class.simpleName ||
                    className == OutputValue::class.simpleName ||
                    className == InputValue::class.simpleName -> {
                    "Any" to emptyList<Projection>()
                }

                // ObjectValue<Xyz> -> Xyz
                className == ObjectValue::class.simpleName -> {
                    val projection = type.projections.firstOrNull()
                    val projectionName =
                        (projection as? Variance<*>)
                            ?.inner
                            ?.driOrNull
                            ?.classNames

                    projectionName?.let { it to emptyList() }
                        ?: return type.unchanged()
                }

                // XyzValue -> Xyz
                className.endsWith(VALUE_SUFFIX) -> {
                    className.removeSuffix(VALUE_SUFFIX) to null
                }

                else -> return type.unchanged()
            }

        return type
            .copy(dri = dri.copy(classNames = newClassName))
            .let { if (newProjections != null) it.copy(projections = newProjections) else it }
            .changed()
    }
}
