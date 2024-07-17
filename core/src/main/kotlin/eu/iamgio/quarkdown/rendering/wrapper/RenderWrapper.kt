package eu.iamgio.quarkdown.rendering.wrapper

import eu.iamgio.quarkdown.util.replace

/**
 * A code wrapper that adds static content to the output code of the rendering stage, and supports injection of values via placeholder keys.
 * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
 * Placeholders in templates are wrapped by double square brackets: `\[\[CONTENT]]` is the default placeholder for content code to inject.
 * See `resources/render` for templates.
 * @param code code of the template
 */
class RenderWrapper(private val code: String) {
    private val placeholders: MutableMap<String, Any> = mutableMapOf()
    private val conditionals: MutableMap<String, Boolean> = mutableMapOf()

    /**
     * Adds a reference to a placeholder in the template code.
     * The placeholder in the template must be wrapped by double square brackets.
     * @param placeholder placeholder to replace
     * @param value value to replace in change of [placeholder]
     * @return this for concatenation
     */
    fun value(
        placeholder: String,
        value: Any,
    ) = apply { placeholders[placeholder] = value }

    /**
     * Adds a conditional variable that shows or removes fragments of the template code.
     * The fragment in the template must be fenced by `[[if:NAME]]` and `[[endif:NAME]]`.
     * An inverted (_not_) placeholder is also injected and can be accessed via `[[if:!NAME]]`.
     * @param conditional conditional name
     * @param value whether the fragment should be shown (`true`) or hidden (`false`)
     * @return this for concatenation
     */
    fun conditional(
        conditional: String,
        value: Boolean,
    ) = apply {
        conditionals[conditional] = value
        conditionals["!$conditional"] = !value // inverted conditional ("not")
    }

    /**
     * Adds both a [conditional] to check if [value] is not `null`,
     * and a [value] to replace the placeholder with the non-`null` value.
     * @param placeholder both placeholder to replace and name of the conditional
     * @param value value to replace in change of the placeholder
     * @return this for concatenation
     * @see conditional
     * @see value
     */
    fun optionalValue(
        placeholder: String,
        value: Any?,
    ) = conditional(placeholder, value != null).value(placeholder, value ?: "")

    /**
     * Adds a reference to a content placeholder in the template code.
     * This is used to inject rendered code in a template.
     * @param content value to replace in change of the `\[\[CONTENT]]` placeholder
     * @return this for concatenation
     */
    fun content(content: CharSequence) = value(TemplatePlaceholders.CONTENT, content)

    /**
     * @return the template, with the supplied placeholders replaced with the actual values
     */
    fun wrap(): CharSequence =
        buildString {
            append(code)

            // Replace conditionals.
            // Just like ifdef macros, conditionals keep or cut content depending on a boolean value.
            // Delimiters are defined as [[if:NAME]]...[[endif:NAME]] in the template files.
            conditionals.forEach { (placeholder, value) ->
                // Regex to find conditional fragments.
                val regex =
                    "\\[\\[if:$placeholder]]((.|\\R)+?)\\[\\[endif:$placeholder]]\\R?".toRegex(RegexOption.MULTILINE)
                // If there is a match:
                // Keep the inner content (without the delimiters) if the conditional value is true, remove it otherwise.
                regex.findAll(this).forEach { match ->
                    replace(
                        match.range.first,
                        match.range.last + 1,
                        // First group is the whole match, second group is the inner content without the delimiters.
                        match.groups[1]?.value?.takeIf { value } ?: "",
                    )
                }
            }

            // Replace placeholders (defined as [[NAME]] in the template file) with their corresponding value.
            placeholders.forEach { (placeholder, value) ->
                replace("[[$placeholder]]", value.toString())
            }
        }

    companion object {
        /**
         * @param name name of the internal resource
         * @return a new [RenderWrapper] with its template loaded from the resource content
         * @throws IllegalStateException if the resource cannot be found
         */
        fun fromResourceName(name: String) =
            RenderWrapper(
                Companion::class.java.getResourceAsStream(name)
                    ?.reader()
                    ?.readText()
                    ?: throw IllegalStateException("Cannot find wrapper resource $name."),
            )
    }
}
