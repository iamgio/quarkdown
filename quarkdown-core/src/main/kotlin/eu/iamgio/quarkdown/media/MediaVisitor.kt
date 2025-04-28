package eu.iamgio.quarkdown.media

/**
 * A visitor for [Media] implementations.
 * @param T return type of the visitor
 */
interface MediaVisitor<T> {
    fun visit(media: LocalMedia): T

    fun visit(media: RemoteMedia): T
}
