package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.quarkdoc.dokka.util.extractAnnotation
import org.jetbrains.dokka.base.transformers.documentables.DocumentableReplacerTransformer
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Nullable
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Utility that extends [DocumentableReplacerTransformer] to allow for easy transformation of functions and parameters
 * while relying on the default behavior of the base class.
 */
open class QuarkdocDocumentableReplacerTransformer(
    context: DokkaContext,
) : DocumentableReplacerTransformer(context) {
    protected fun <T> T.changed(changed: Boolean = true) = AnyWithChanges(this, changed = changed)

    protected fun <T> T.unchanged() = changed(changed = false)

    private fun <T> AnyWithChanges<T>.merge(other: AnyWithChanges<T>): AnyWithChanges<T> =
        AnyWithChanges(
            target = other.target,
            changed = this.changed || other.changed,
        )

    protected fun <T> AnyWithChanges<T>.merge(other: (T) -> AnyWithChanges<T>): AnyWithChanges<T> = this.merge(other(this.target!!))

    protected open fun transformFunction(function: DFunction) = function.unchanged()

    override fun processFunction(dFunction: DFunction) = super.processFunction(dFunction).merge(::transformFunction)

    protected open fun transformParameter(parameter: DParameter) = parameter.unchanged()

    override fun processParameter(dParameter: DParameter) = super.processParameter(dParameter).merge(::transformParameter)

    protected open fun transformType(type: GenericTypeConstructor) = type.unchanged()

    override fun processGenericTypeConstructor(genericTypeConstructor: GenericTypeConstructor) =
        super.processGenericTypeConstructor(genericTypeConstructor).merge(::transformType)

    // Dokka's DocumentableReplacerTransformer implementation does not propagate into Nullable types for some reason.

    private fun transformNullableType(nullable: Nullable) =
        super
            .processBound(nullable.inner)
            .takeIf { it.changed }
            ?.let { nullable.copy(inner = it.target!!).changed() }
            ?: nullable.unchanged()

    override fun processBound(bound: Bound): AnyWithChanges<Bound> =
        super
            .processBound(bound)
            .merge {
                when (it) {
                    is Nullable -> transformNullableType(it)
                    else -> it.unchanged()
                }
            }

    /**
     * @return the optional overridden name of the function or parameter, or `null` if not annotated with `@Name`.
     */
    protected fun getOverriddenName(documentable: Documentable): String? {
        val nameAnnotation = documentable.extractAnnotation<Name>()
        return nameAnnotation?.params?.get("name")?.toString()
    }
}
