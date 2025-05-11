package com.quarkdown.quarkdoc.dokka.signature

import org.jetbrains.dokka.base.signatures.KotlinSignatureProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Projection
import kotlin.reflect.jvm.isAccessible

/**
 * Helper class to invoke [KotlinSignatureProvider] private build methods via reflection.
 */
class KotlinSignatureReflectionHelper(
    private val kotlin: KotlinSignatureProvider,
) {
    /**
     * Invokes a private build method on [kotlin] using reflection.
     * @param methodName the name of the method to invoke
     * @param args the arguments to pass
     */
    private fun PageContentBuilder.DocumentableContentBuilder.invokeKotlinSignatureBuilderMethod(
        methodName: String,
        vararg args: Any,
    ) {
        kotlin::class
            .members
            .find { it.name == methodName }
            ?.apply { isAccessible = true }
            ?.call(kotlin, this, *args)
    }

    /**
     * Adds a projection/type signature to [this] builder.
     */
    fun PageContentBuilder.DocumentableContentBuilder.projectionSignature(projection: Projection) {
        invokeKotlinSignatureBuilderMethod(
            "signatureForProjection",
            projection,
            false,
        )
    }

    /**
     * Adds a parameter default value assignment to [this] builder.
     */
    fun PageContentBuilder.DocumentableContentBuilder.defaultValue(parameter: DParameter) {
        parameter.sourceSets.forEach { sourceSet ->
            invokeKotlinSignatureBuilderMethod("defaultValueAssign", parameter, sourceSet)
        }
    }
}
