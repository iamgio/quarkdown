package eu.iamgio.quarkdown.rendering.wrapper

import eu.iamgio.quarkdown.util.replace
import java.io.InputStreamReader

/**
 * A code wrapper that adds static content to the output code of the rendering stage, and supports injection of values via placeholder keys.
 * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
 * Placeholders in templates are wrapped by double square brackets: `\[\[CONTENT]]` is the default placeholder for content code to inject.
 * See `resources/render` for templates.
 * @param code template code
 */
class RenderWrapper(private val code: String) {
    private val placeholders: MutableMap<String, Any> = mutableMapOf()

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
     * Adds a reference to a content placeholder in the template code.
     * This is used to inject rendered code in a template.
     * @param content value to replace in change of the `\[\[CONTENT]]` placeholder
     * @return this for concatenation
     * @see CONTENT_PLACEHOLDER
     */
    fun content(content: CharSequence) = value(TemplatePlaceholders.CONTENT, content)

    /**
     * @return the template, with the supplied placeholders replaced with the actual values
     */
    fun wrap(): CharSequence =
        buildString {
            append(code)
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
                InputStreamReader(
                    Companion::class.java.getResourceAsStream(name)
                        ?: throw IllegalStateException("Cannot find wrapper resource $name."),
                ).readText(),
            )
    }
}
