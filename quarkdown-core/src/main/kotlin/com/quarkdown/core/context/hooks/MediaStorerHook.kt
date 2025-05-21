package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.media.StoredMediaProperty
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.log.Log
import com.quarkdown.core.media.storage.MutableMediaStorage
import com.quarkdown.core.media.storage.StoredMedia
import java.io.File

/**
 * Hook that, when a node containing information about media is found,
 * registers it in the media [storage].
 * A media storage is a temporary lookup table that maps media to their paths, so that they can be resolved later.
 * @param storage media storage
 * @param workingDirectory directory from which media are resolved, in case they use relative paths
 */
class MediaStorerHook(
    private val storage: MutableMediaStorage,
    private val attributes: MutableAstAttributes,
    private val workingDirectory: File?,
) : AstIteratorHook {
    constructor(context: MutableContext) : this(
        context.mediaStorage,
        context.attributes,
        context.attachedPipeline?.options?.workingDirectory,
    )

    /**
     * Registers a media contained within a link into the media storage
     * and attaches the new media to the node's extra attributes.
     * @param link the link node containing the media to register.
     * It is also the node to attach the [StoredMediaProperty] to, into [com.quarkdown.core.ast.attributes.AstAttributes.properties]
     */
    private fun register(link: LinkNode) {
        val media: StoredMedia? =
            try {
                storage.register(link.url, workingDirectory)
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
                attributes.of(link) += it
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
