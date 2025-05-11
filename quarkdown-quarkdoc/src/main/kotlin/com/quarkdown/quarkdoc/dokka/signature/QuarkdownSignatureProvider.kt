package com.quarkdown.quarkdoc.dokka.signature

import com.quarkdown.quarkdoc.dokka.storage.QuarkdownModulesStorage
import com.quarkdown.quarkdoc.dokka.util.documentableContentBuilder
import org.jetbrains.dokka.base.signatures.KotlinSignatureProvider
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.TokenStyle
import org.jetbrains.dokka.plugability.DokkaContext
import kotlin.reflect.jvm.isAccessible

private const val BEGIN = "."
private const val INLINE_PARAMETER_START = "{"
private const val INLINE_PARAMETER_END = "}"
private const val INLINE_PARAMETER_DELIMITER = " "
private const val PARAMETER_TYPE_DELIMITER = ": "
private const val RETURN_TYPE_DELIMITER = " -> "

/**
 *
 */
class QuarkdownSignatureProvider(
    private val context: DokkaContext,
) : SignatureProvider {
    private val kotlin = KotlinSignatureProvider(context)

    override fun signature(documentable: Documentable): List<ContentNode> {
        if (!QuarkdownModulesStorage.isModule(documentable)) {
            return kotlin.signature(documentable)
        }

        val builder =
            context.documentableContentBuilder(
                documentable,
                setOf(documentable.dri),
            )

        return when (documentable) {
            is DFunction ->
                builder
                    .buildGroup {
                        codeBlock { signature(documentable) }
                    }.children

            else -> kotlin.signature(documentable)
        }
    }

    private fun PageContentBuilder.DocumentableContentBuilder.signature(function: DFunction) {
        punctuation(BEGIN)
        text(function.name, styles = setOf(TokenStyle.Function))
        function.parameters.forEach { parameter ->
            punctuation(INLINE_PARAMETER_DELIMITER)
            punctuation(INLINE_PARAMETER_START)
            text(parameter.name ?: "")
            operator(PARAMETER_TYPE_DELIMITER)
            +projectionSignature(parameter.type)
            punctuation(INLINE_PARAMETER_END)
        }
        operator(RETURN_TYPE_DELIMITER)
        +projectionSignature(function.type)
    }

    @Suppress("UNCHECKED_CAST")
    private fun PageContentBuilder.DocumentableContentBuilder.projectionSignature(projection: Projection): List<ContentNode> {
        // Invokes the private method `signatureForProjection` from the KotlinSignatureProvider.
        return kotlin::class
            .members
            .find { it.name == "signatureForProjection" }
            ?.apply { isAccessible = true }
            ?.call(kotlin, this, projection, false) as? List<ContentNode>
            ?: emptyList()
    }
}
