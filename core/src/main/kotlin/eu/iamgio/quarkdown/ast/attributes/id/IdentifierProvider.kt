package eu.iamgio.quarkdown.ast.attributes.id

import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure

/**
 * Provides identifiers for [Identifiable] elements.
 * Usually, an implementation is provided for each rendering target.
 * For example, HTML identifiers are URI-like.
 * @param T output type of the identifiers
 * @see eu.iamgio.quarkdown.rendering.html.HtmlIdentifierProvider
 */
interface IdentifierProvider<T> {
    fun visit(heading: Heading): T

    fun visit(figure: ImageFigure): T
}

/**
 * Gets the identifier of an [Identifiable] element.
 * @param identifiable element to get the identifier of
 * @return identifier of the element provided by [this] provider
 */
fun <T> IdentifierProvider<T>.getId(identifiable: Identifiable) = identifiable.accept(this)
