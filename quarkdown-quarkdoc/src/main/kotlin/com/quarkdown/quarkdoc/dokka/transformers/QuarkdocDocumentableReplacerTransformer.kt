package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.base.transformers.documentables.DocumentableReplacerTransformer
import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DProperty
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

    protected open fun transformModule(module: DModule) = module.unchanged()

    override fun processModule(module: DModule) = super.processModule(module).merge(::transformModule)

    protected open fun transformPackage(pkg: DPackage) = pkg.unchanged()

    override fun processPackage(dPackage: DPackage) = super.processPackage(dPackage).merge(::transformPackage)

    protected open fun transformClassLike(classlike: DClasslike) = classlike.unchanged()

    override fun processClassLike(classlike: DClasslike) = super.processClassLike(classlike).merge(::transformClassLike)

    protected open fun transformProperty(property: DProperty) = property.unchanged()

    override fun processProperty(dProperty: DProperty) = super.processProperty(dProperty).merge(::transformProperty)

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
}
