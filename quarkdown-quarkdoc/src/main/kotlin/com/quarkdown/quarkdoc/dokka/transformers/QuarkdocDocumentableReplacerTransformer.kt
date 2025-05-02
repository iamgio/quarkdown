package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.base.transformers.documentables.DocumentableReplacerTransformer
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Utility that extends [DocumentableReplacerTransformer] to allow for easy transformation of functions and parameters
 * while relying on the default behavior of the base class.
 */
open class QuarkdocDocumentableReplacerTransformer(
    context: DokkaContext,
) : DocumentableReplacerTransformer(context) {
    private fun <D : Documentable> D.unmodified() = AnyWithChanges(this, changed = false)

    private fun <T> AnyWithChanges<T>.merge(other: AnyWithChanges<T>): AnyWithChanges<T> =
        AnyWithChanges(
            target = other.target,
            changed = this.changed || other.changed,
        )

    private fun <T> AnyWithChanges<T>.merge(other: (T) -> AnyWithChanges<T>): AnyWithChanges<T> = this.merge(other(this.target!!))

    protected open fun transformFunction(function: DFunction) = function.unmodified()

    override fun processFunction(dFunction: DFunction) = super.processFunction(dFunction).merge(::transformFunction)

    protected open fun transformParameter(parameter: DParameter) = parameter.unmodified()

    override fun processParameter(dParameter: DParameter) = super.processParameter(dParameter).merge(::transformParameter)
}
