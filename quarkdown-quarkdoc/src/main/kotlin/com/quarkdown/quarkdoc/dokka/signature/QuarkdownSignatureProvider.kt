package com.quarkdown.quarkdoc.dokka.signature

import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.quarkdoc.dokka.transformers.module.QuarkdownModulesStorage
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

internal const val BEGIN = FunctionCallGrammar.BEGIN
internal const val CHAINING_DELIMITER = FunctionCallGrammar.CHAIN_SEPARATOR
private const val INLINE_PARAMETER_START = FunctionCallGrammar.ARGUMENT_BEGIN.toString()
private const val INLINE_PARAMETER_END = FunctionCallGrammar.ARGUMENT_END.toString()
private const val INLINE_PARAMETER_DELIMITER = " "
private const val PARAMETER_NAME_DELIMITER = FunctionCallGrammar.NAMED_ARGUMENT_DELIMITER
private const val RETURN_TYPE_DELIMITER = "-> "

/**
 * Signature provider for Quarkdown functions.
 * @param requireModule whether to require the function to be in a Quarkdown module
 * @param defaultValues whether to include default values for arguments
 * @param withChaining whether to format the signature for chained calls
 */
class QuarkdownSignatureProvider(
    private val context: DokkaContext,
    private val requireModule: Boolean = true,
    private val defaultValues: Boolean = true,
    private val withChaining: Boolean = false,
) : SignatureProvider {
    private val kotlin = KotlinSignatureProvider(context)
    private val helper = KotlinSignatureReflectionHelper(kotlin)

    override fun signature(documentable: Documentable): List<ContentNode> {
        if (requireModule && !QuarkdownModulesStorage.isModule(documentable)) {
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
            // Without chaining: .func param1:{Type1} param2:{Type2}
            // With chaining: Type1::func param2:{Type2}
            if (withChaining && function.parameters.isNotEmpty()) {
                projectionSignature(function.parameters.first().type)
                punctuation(CHAINING_DELIMITER)
            } else {
                punctuation(BEGIN)
            }

            text(function.name, styles = setOf(TokenStyle.Function))

            val lineBreakingStrategy = LineBreakingStrategy.fromFunction(function)
            val parameters =
                when {
                    withChaining -> function.parameters.drop(1)
                    else -> function.parameters
                }
            parameters.forEachIndexed { index, parameter ->
                lineBreakingStrategy.run { beforeParameter(parameter, index) }
                signature(parameter)
            }

            lineBreakingStrategy.run { beforeReturn() }
            operator(RETURN_TYPE_DELIMITER)
            projectionSignature(function.type)
        }

    private fun PageContentBuilder.DocumentableContentBuilder.signature(parameter: DParameter) =
        with(helper) {
            punctuation(INLINE_PARAMETER_DELIMITER)
            constant(parameter.name ?: "<unnamed>")
            operator(PARAMETER_NAME_DELIMITER)
            punctuation(INLINE_PARAMETER_START)
            projectionSignature(parameter.type)
            if (defaultValues) defaultValue(parameter)
            punctuation(INLINE_PARAMETER_END)
        }
}
