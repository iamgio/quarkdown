package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.stdlib.internal.applyImportantToCSS

/**
 * `Injection` stdlib module exporter.
 * This module handles code injection of different languages.
 */
val Injection: QuarkdownModule =
    moduleOf(
        ::html,
        ::css,
        ::cssProperties,
    )

/**
 * Creates an HTML element, which is rendered as-is without any additional processing or escaping,
 * as long as the rendering target supports HTML.
 *
 * ```html
 * .html
 *     <div class="my-container">
 *       My HTML container
 *     </div>
 * ```
 *
 * ```markdown
 * **Hello** .html {<em>world</em>}!
 * ```
 *
 * @param content raw HTML content to inject
 * @return a new [Html] node
 * @wiki HTML
 */
fun html(
    @LikelyBody content: String,
) = Html(content).wrappedAsValue()

/**
 * Creates a `<style>` HTML element with the provided CSS content.
 * The content is wrapped in a `<style>` tag and rendered as-is,
 * without any additional processing or escaping, as long as the rendering target supports HTML.
 *
 * Each CSS property value automatically has `!important` applied to it,
 * unless it already has it.
 *
 * ```css
 * .css
 *   body {
 *     background-color: green;
 *   }
 * ```
 *
 * @param content raw CSS content to inject
 * @return a new [Html] node representing the style element
 * @see [cssProperties] for a more structured way to override CSS properties.
 * @wiki CSS
 */
fun css(
    @LikelyBody content: String,
) = Html("<style data-hidden=\"\">${applyImportantToCSS(content)}</style>").wrappedAsValue()

private const val CSS_ROOT_SELECTOR = ":root"
private const val CSS_PROPERTY_PREFIX = "--qd-"

/**
 * Overrides the value of Quarkdown CSS properties.
 *
 * Each entry corresponds to a Quarkdown CSS property name and its value.
 * The names will be prefixed with `--qd-` to match the Quarkdown CSS variable naming convention.
 *
 * ```yaml
 * .cssproperties
 *   - background-color: green
 *   - main-font-size: 20px
 * ```
 *
 * For a complete list of properties, see the [global theme](https://github.com/iamgio/quarkdown/blob/main/quarkdown-html/src/main/scss/global.scss).
 * Unknown properties will be ignored.
 *
 * The content is wrapped in a `<style>` tag and rendered as-is,
 * without any additional processing or escaping, as long as the rendering target supports HTML.
 *
 * @param properties a dictionary of CSS property names and their values
 * @return a new [Html] node representing the style element
 * @wiki CSS
 */
@Name("cssproperties")
fun cssProperties(properties: Map<String, Value<*>>) =
    css(
        buildString {
            append(CSS_ROOT_SELECTOR)
            append(" { ")
            properties.forEach { (name, value) ->
                append("$CSS_PROPERTY_PREFIX$name: ${value.unwrappedValue}; ")
            }
            append("}")
        },
    )
