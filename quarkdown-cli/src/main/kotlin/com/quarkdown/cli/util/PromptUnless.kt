package com.quarkdown.cli.util

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionCallTransformContext
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.transform.terminal
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.prompt

/**
 * Like Clikt's `prompt`, but the prompt is skipped when [fallback] yields a value, which is then used
 * as if the user had passed it. Resolution order: the value given on the command line, then [fallback],
 * then the interactive prompt. The typed answer, as well as [default], goes through the option's own conversion,
 * so validation and error messages match the command-line form; an invalid answer is asked again.
 * A closed input yields [default]. Like Clikt's `prompt`, no prompt is shown once another parameter has already failed.
 * @param text prompt text, shown with the default appended when the default is not blank
 * @param default value to use on an empty answer or a closed input, written as the user would type it
 * @param fallback supplier of the value that skips the prompt, or `null` to prompt
 * @return the option with the prompt behavior attached
 */
fun <T : Any> NullableOption<T, T>.promptUnless(
    text: String,
    default: String,
    fallback: () -> T?,
): OptionWithValues<T, T, T> {
    val convert = transformValue
    return transformAll { invocations ->
        invocations.lastOrNull()
            ?: fallback()
            ?: run {
                if (context.errorEncountered) throw Abort()
                val callContext = OptionCallTransformContext(option.names.first(), option, context)
                val convertAnswer: (String) -> ConversionResult<T> = { answer ->
                    try {
                        ConversionResult.Valid(convert(callContext, answer.ifBlank { default }))
                    } catch (e: UsageError) {
                        ConversionResult.Invalid(e.message ?: "Invalid value")
                    }
                }
                terminal.prompt(promptLabel(text, default), default = null, showDefault = false, convert = convertAnswer)
                    ?: convert(callContext, default)
            }
    }
}

/**
 * Like Clikt's `prompt`, but a closed input, such as a non-interactive shell or a pipe at its end,
 * yields [default] instead of failing with a missing-option error.
 * @param text prompt text, shown with the default appended when the default is not blank
 * @param default value to use on an empty answer or a closed input, written as the user would type it
 * @return the option with the prompt behavior attached
 * @see promptUnless
 */
fun <T : Any> NullableOption<T, T>.promptOrDefault(
    text: String,
    default: String = "",
): OptionWithValues<T, T, T> = promptUnless(text, default) { null }

private fun promptLabel(
    text: String,
    default: String,
): String = if (default.isBlank()) text else "$text ${dim("($default)")}"
