package eu.iamgio.quarkdown.ast.id

import eu.iamgio.quarkdown.ast.base.block.Heading

/**
 * An element that can be identified by a unique identifier, referenced and located by other elements in a document.
 */
interface Identifiable {
    /**
     * Accepts an [IdentifierProvider] to generate an identifier for this element.
     * @param visitor visitor to accept
     * @param T output type of the provider
     */
    fun <T> accept(visitor: IdentifierProvider<T>): T
}

/**
 * Provides identifiers for [Identifiable] elements.
 * Usually, an implementation is provided for each rendering target.
 * For example, HTML identifiers are URI-like.
 * @param T output type of the identifiers
 * @see eu.iamgio.quarkdown.rendering.html.HtmlIdentifierProvider
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
