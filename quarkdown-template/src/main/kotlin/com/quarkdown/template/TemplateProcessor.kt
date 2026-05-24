package com.quarkdown.template

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput

/**
 * A builder-like processor for a JTE (Java Template Engine) `.jte` template, identified by its
 * precompiled template [name] (e.g. `creator/main.qd.jte`). Templates must be precompiled at
 * build time via the JTE Gradle plugin and shipped on the runtime classpath; see the
 * `quarkdown-templates` module for production templates.
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
 * @param name template name as known by the precompiled JTE engine, relative to its source root
 */
class TemplateProcessor(
    private val name: String,
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
     * @param name template name for the new processor; defaults to this processor's [name]
     * @return a new [TemplateProcessor] with the same injections, and the new template name
     */
    fun copy(name: String = this.name) =
        TemplateProcessor(
            name,
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
     * @return the rendered template, with all placeholders and conditionals processed into the final output
     */
    fun process(): CharSequence {
        val output = StringOutput()
        engine.render(name, buildParams(), output)
        return output.toString().trimEnd()
    }

    companion object {
        /**
         * Shared, precompiled JTE engine. Templates are resolved from the runtime classpath
         * (the `quarkdown-templates` module's jar in production, test-source `.jte` fixtures
         * during tests).
         */
        private val engine: TemplateEngine =
            TemplateEngine
                .createPrecompiled(ContentType.Plain)
                .apply { setTrimControlStructures(true) }
    }
}
