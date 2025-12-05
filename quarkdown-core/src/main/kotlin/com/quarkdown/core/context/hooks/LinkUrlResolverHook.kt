package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.link.setResolvedUrl
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.util.isURL
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Hook that resolves relative link paths based on their file system.
 *
 * If a link uses a relative path and its file system
 * is different from the [context]'s file system,
 * the path is resolved relative to the context's file system.
 *
 * This is mainly applied to images.
 *
 * @param context root context to use for resolution
 * @see com.quarkdown.core.ast.attributes.link.ResolvedImagePathProperty
 */
class LinkUrlResolverHook(
    private val context: MutableContext,
) : AstIteratorHook {
    /**
     * Resolves the URL of a [link] if it's a relative path
     * and its file system is different from the [context]'s file system.
     *
     * @param link link node to resolve
     */
    private fun resolve(link: LinkNode) {
        val fileSystem = link.fileSystem
        if (fileSystem == null || fileSystem.isRoot) return // No need to resolve paths.

        if (link.url.isURL || Path(link.url).isAbsolute) return // Not a relative path.

        val resolved: Path? =
            context.fileSystem
                .relativePathTo(fileSystem)
                ?.resolve(link.url)
                ?.normalize()

        resolved?.let {
            link.setResolvedUrl(context, it.toString())
        }
    }

    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Image> { resolve(it.link) }
        iterator.on<LinkDefinition> { resolve(it) }
    }
}
