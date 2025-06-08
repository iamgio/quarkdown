package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Injection` stdlib module exporter.
 * This module handles code injection of different languages.
 */
val Injection: Module =
    moduleOf(
        ::html,
    )

/**
 * Creates an HTML element, which is rendered as-is without any additional processing or escaping.
 * @param content raw HTML content to inject
 * @return a new [Html] node
 * @wiki HTML
 */
fun html(
    @LikelyBody content: String,
) = Html(content).wrappedAsValue()
