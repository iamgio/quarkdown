package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.link.setResolvedUrl
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.util.isURL
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Hook that resolves relative image paths based on their file system.
 *
 * If an image's link uses a relative path and its file system
 * is different from the [context]'s file system,
 * the path is resolved relative to the context's file system.
 *
 * @param context root context to use for resolution
 * @see com.quarkdown.core.ast.attributes.link.ResolvedImagePathProperty
 */
class ImagePathResolverHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Image> { image ->
            val fileSystem = (image.link as? Link)?.fileSystem
            if (fileSystem == null || fileSystem.isRoot) return@on // No need to resolve relative paths.

            val url = image.link.url
            if (url.isURL || Path(url).isAbsolute) return@on

            val resolved: Path? =
                context.fileSystem
                    .relativePathTo(fileSystem)
                    ?.resolve(url)
                    ?.normalize()

            resolved?.let {
                image.setResolvedUrl(context, it.toString())
            }
        }
    }
}
