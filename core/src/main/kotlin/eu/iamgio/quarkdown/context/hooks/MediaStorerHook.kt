package eu.iamgio.quarkdown.context.hooks

import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.ReferenceImage
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.media.storage.MutableMediaStorage
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
    private val workingDirectory: File?,
) : AstIteratorHook {
    constructor(context: MutableContext) : this(
        context.mediaStorage,
        context.attachedPipeline?.options?.workingDirectory,
    )

    override fun attach(iterator: ObservableAstIterator) {
        // Registers the media, wrapped in a link, to the media storage.
        fun register(link: LinkNode) = storage.register(link.url, workingDirectory)

        // Images are instantly registered.
        iterator.on<Image> { register(it.link) }

        // Reference images are registered upon resolution,
        // i.e. when a definition that matches the reference is found.
        iterator.on<ReferenceImage> { it.link.onResolve.add(::register) }
    }
}
