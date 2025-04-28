package eu.iamgio.quarkdown.ast.attributes.id

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
