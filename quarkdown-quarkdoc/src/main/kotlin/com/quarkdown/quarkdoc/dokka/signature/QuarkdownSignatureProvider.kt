package com.quarkdown.quarkdoc.dokka.signature

import com.quarkdown.quarkdoc.dokka.storage.QuarkdownModulesStorage
import com.quarkdown.quarkdoc.dokka.util.documentableContentBuilder
import org.jetbrains.dokka.base.signatures.KotlinSignatureProvider
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.TokenStyle
import org.jetbrains.dokka.plugability.DokkaContext

internal const val BEGIN = "."
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
    private val helper = KotlinSignatureReflectionHelper(kotlin)

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

    private fun PageContentBuilder.DocumentableContentBuilder.signature(function: DFunction) =
        with(helper) {
            val lineBreakingStrategy = LineBreakingStrategy.fromFunction(function)
            punctuation(BEGIN)
            text(function.name, styles = setOf(TokenStyle.Function))

            function.parameters.forEachIndexed { index, parameter ->
                lineBreakingStrategy.run { beforeParameter(index) }
                signature(parameter)
            }

            lineBreakingStrategy.run { beforeReturn() }
            operator(RETURN_TYPE_DELIMITER)
            projectionSignature(function.type)
        }

    private fun PageContentBuilder.DocumentableContentBuilder.signature(parameter: DParameter) =
        with(helper) {
            punctuation(INLINE_PARAMETER_DELIMITER)
            punctuation(INLINE_PARAMETER_START)
            text(parameter.name ?: "<unnamed>")
            operator(PARAMETER_TYPE_DELIMITER)
            projectionSignature(parameter.type)
            defaultValue(parameter)
            punctuation(INLINE_PARAMETER_END)
        }
}
