package com.quarkdown.core.template

import com.quarkdown.core.util.normalizeLineSeparators
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import kotlin.io.path.createTempDirectory

/**
 * A builder-like processor for a template engine backed by JTE (Java Template Engine) with `.kte` Kotlin templates.
 *
 * Three main features:
 *
 * - **Values**: replace a placeholder in the template with a value.
 *   In JTE templates, values are referenced via `${NAME}`.
 *
 * - **Conditionals**: show or hide fragments of the template code.
 *   In JTE templates, conditionals are expressed via `@if(NAME)...@endif`.
 *   An inverted (_not_) conditional is expressed via `@if(!NAME)...@endif`.
 *
 * - **Iterables**: repeat the content in their fragment as many times as the iterable's size,
 *   while replacing the placeholder with the current item during each iteration.
 *   In JTE templates, iterables are expressed via `@for(item in NAME)${item}@endfor`.
 *
 * @param text text or code of the `.kte` template
 */
class TemplateProcessor(
    private val text: String,
    private val values: MutableMap<String, Any?> = mutableMapOf(),
    private val conditionals: MutableMap<String, Boolean> = mutableMapOf(),
    private val iterables: MutableMap<String, Iterable<Any>> = mutableMapOf(),
) {
    /**
     * Adds a reference to a placeholder in the template code.
     * @param placeholder placeholder to replace
     * @param value value to replace in change of [placeholder]
     * @return this for concatenation
     */
    fun value(
        placeholder: String,
        value: Any?,
    ) = apply { values[placeholder] = value }

    /**
     * Adds a conditional variable that shows or removes fragments of the template code.
     * @param conditional conditional name
     * @param value whether the fragment should be shown (`true`) or hidden (`false`)
     * @return this for concatenation
     */
    fun conditional(
        conditional: String,
        value: Boolean,
    ) = apply {
        conditionals[conditional] = value
    }

    /**
     * Adds both a [value] to replace the placeholder (or `null` if absent),
     * and registers it so that the template can check for `null` presence.
     * @param placeholder both placeholder to replace and name of the conditional
     * @param value value to replace in change of the placeholder, or `null` if absent
     * @return this for concatenation
     * @see value
     */
    fun optionalValue(
        placeholder: String,
        value: Any?,
    ) = value(placeholder, value)

    /**
     * Adds an iterable to replace a placeholder in the template code.
     * @param placeholder placeholder to replace
     * @param iterable iterable to replace in change of [placeholder]
     * @return this for concatenation
     */
    fun iterable(
        placeholder: String,
        iterable: Iterable<Any>,
    ) = apply { iterables[placeholder] = iterable }

    /**
     * Creates a copy of this template processor with the same injected properties.
     * @param text new text the template
     * @return a new [TemplateProcessor] with the same injections, and the new text
     */
    fun copy(text: String = this.text) =
        TemplateProcessor(
            text,
            values.toMutableMap(),
            conditionals.toMutableMap(),
            iterables.toMutableMap(),
        )

    /**
     * Builds a unified parameter map from the registered values, conditionals, and iterables,
     * suitable for passing to the JTE template engine.
     *
     * When a key exists in multiple maps, values and iterables take precedence over conditionals,
     * since templates can check values for `null` and iterables for emptiness directly,
     * making the standalone boolean redundant.
     */
    private fun buildParams(): Map<String, Any?> =
        buildMap {
            putAll(conditionals)
            putAll(this@TemplateProcessor.values)
            putAll(iterables)
        }

    /**
     * @return the original template [text], with all placeholders and conditionals processed into the final output
     */
    fun process(): CharSequence {
        val normalizedText = text.normalizeLineSeparators().toString()
        val params = buildParams()

        val templateName = "template.kte"
        val codeResolver =
            object : CodeResolver {
                override fun resolve(name: String): String = normalizedText

                override fun getLastModified(name: String): Long = 0L

                override fun exists(name: String): Boolean = name == templateName
            }

        val engine =
            TemplateEngine.create(
                codeResolver,
                createTempDirectory("jte"),
                ContentType.Plain,
            )
        engine.setTrimControlStructures(true)
        val output = StringOutput()
        engine.render(templateName, params, output)
        return output.toString().trimEnd()
    }

    companion object {
        /**
         * @param name name of the internal resource
         * @param referenceClass reference classpath to use to retrieve the internal resource
         * @return a new [TemplateProcessor] with its template loaded from the resource content
         * @throws IllegalStateException if the resource cannot be found
         */
        fun fromResourceName(
            name: String,
            referenceClass: Class<*> = TemplateProcessor::class.java,
        ) = TemplateProcessor(
            referenceClass
                .getResourceAsStream(name)
                ?.reader()
                ?.readText()
                ?: throw IllegalStateException("Cannot find wrapper resource $name."),
        )
    }
}
