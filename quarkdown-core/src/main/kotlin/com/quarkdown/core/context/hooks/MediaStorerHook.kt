package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.link.getResolvedUrl
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.media.StoredMediaProperty
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.log.Log
import com.quarkdown.core.media.storage.StoredMedia

/**
 * Hook that, when a node containing information about media is found,
 * registers it in the media [storage].
 * A media storage is a temporary lookup table that maps media to their paths, so that they can be resolved later.
 * @param storage media storage where media are registered
 */
class MediaStorerHook(
    private val context: MutableContext,
) : AstIteratorHook {
    /**
     * Registers a media contained within a link into the media storage
     * and attaches the new media to the node's extra attributes.
     *
     * [getResolvedUrl] is used rather than [LinkNode.url] in case a different URL was set by [LinkUrlResolverHook].
     *
     * @param link the link node containing the media to register.
     * It is also the node to attach the [StoredMediaProperty] to, into [com.quarkdown.core.ast.attributes.AstAttributes.properties]
     */
    private fun register(link: LinkNode) {
        val media: StoredMedia? =
            try {
                context.mediaStorage.register(
                    link.getResolvedUrl(context),
                    context.fileSystem.workingDirectory,
                )
            } catch (_: IllegalArgumentException) {
                // If the media cannot be resolved, it is ignored and not stored.
                Log.warn("Media cannot be resolved: ${link.url}")
                return
            }

        // The stored media is attached to the node's extra attributes.
        media
            ?.let(::StoredMediaProperty)
            ?.also { Log.debug("Registered media: ${link.url} -> ${it.value}") }
            ?.let {
                context.attributes.of(link) += it
            }
    }

    override fun attach(iterator: ObservableAstIterator) {
        // Images are instantly registered.
        iterator.on<Image> { register(it.link) }

        // Reference images are registered upon resolution,
        // i.e. when a definition that matches the reference is found.
        iterator.on<ReferenceImage> { it.link.onResolve.add(::register) }
    }
}
