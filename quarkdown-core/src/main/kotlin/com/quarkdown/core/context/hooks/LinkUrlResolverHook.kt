package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.attributes.link.setResolvedUrl
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.FsPaths
import com.quarkdown.core.media.passthrough.MediaPassthrough
import com.quarkdown.core.util.isURL

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
 * @see com.quarkdown.core.ast.attributes.link.ResolvedLinkUrlProperty
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
        if (MediaPassthrough.isPassthroughPath(link.url)) return // No need to resolve passthrough paths.
        if (link.url.isURL || FsPaths.isAbsolute(link.url)) return // Not a relative path.

        val resolved: FsEntry? =
            context.fileSystem
                .relativePathTo(fileSystem)
                ?.resolve(link.url)
                ?.normalized

        resolved?.let {
            link.setResolvedUrl(context, it.invariantSeparatorsPath)
        }
    }

    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Image> { resolve(it.link) }
        iterator.on<LinkDefinition> { resolve(it) }
    }
}
