package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.stdlib.internal.applyImportantToCSS

/**
 * `Injection` stdlib module exporter.
 * This module handles code injection of different languages.
 */
val Injection: QuarkdownModule =
    moduleOf(
        ::htmlOptions,
        ::html,
        ::css,
        ::cssProperties,
    )

/**
 * Configures HTML generation options.
 *
 * ```markdown
 * .htmloptions baseurl:{https://example.com}
 * ```
 *
 * @param baseUrl the base URL to use for resolving relative paths in the generated HTML, e.g. `https://example.com`.
 *                Trailing slashes are automatically ignored.
 *                If specified, a canonical link is set in the HTML's `<head>`.
 * @wiki html-options
 */
@Name("htmloptions")
fun htmlOptions(
    @Injected context: MutableContext,
    @Name("baseurl") baseUrl: String? = null,
) = VoidValue.also {
    context.options.html =
        context.options.html.copy(
            baseUrl = baseUrl?.trimEnd('/'),
        )
}

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
 * @permission [Permission.NativeContent] to inject native HTML content
 * @throws com.quarkdown.core.permissions.MissingPermissionException if [Permission.NativeContent] is not granted
 * @wiki html
 */
fun html(
    @Injected context: Context,
    @LikelyBody content: String,
): NodeValue {
    context.requirePermission(
        Permission.NativeContent,
        message = "Cannot inject native HTML content",
    )
    return Html(content).wrappedAsValue()
}

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
 * @permission [Permission.NativeContent] to inject native CSS content
 * @throws com.quarkdown.core.permissions.MissingPermissionException if [Permission.NativeContent] is not granted
 * @see [cssProperties] for a more structured way to override CSS properties.
 * @wiki css
 */
fun css(
    @Injected context: Context,
    @LikelyBody content: String,
) = html(
    context,
    "<style data-hidden=\"\">${applyImportantToCSS(content)}</style>",
)

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
 * @permission [Permission.NativeContent] to inject native CSS properties
 * @throws com.quarkdown.core.permissions.MissingPermissionException if [Permission.NativeContent] is not granted
 * @wiki css
 */
@Name("cssproperties")
fun cssProperties(
    @Injected context: Context,
    properties: Map<String, Value<*>>,
) = css(
    context,
    buildString {
        append(CSS_ROOT_SELECTOR)
        append(" { ")
        properties.forEach { (name, value) ->
            append("$CSS_PROPERTY_PREFIX$name: ${value.unwrappedValue}; ")
        }
        append("}")
    },
)
