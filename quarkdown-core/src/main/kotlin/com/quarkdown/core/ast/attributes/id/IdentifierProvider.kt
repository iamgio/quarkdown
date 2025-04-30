package com.quarkdown.core.ast.attributes.id

import com.quarkdown.core.ast.base.block.Heading

/**
 * Provides identifiers for [Identifiable] elements.
 * Usually, an implementation is provided for each rendering target.
 * For example, HTML identifiers are URI-like.
 * @param T output type of the identifiers
 * @see com.quarkdown.core.rendering.html.HtmlIdentifierProvider
 */
interface IdentifierProvider<T> {
    fun visit(heading: Heading): T
}

/**
 * Gets the identifier of an [Identifiable] element.
 * @param identifiable element to get the identifier of
 * @return identifier of the element provided by [this] provider
 */
fun <T> IdentifierProvider<T>.getId(identifiable: Identifiable) = identifiable.accept(this)
