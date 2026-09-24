package com.quarkdown.rendering.plaintext.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.plaintext.node.PlainTextNodeRenderer
import com.quarkdown.rendering.plaintext.post.PlainTextPostRenderer

/**
 * The plain-text rendering plug-in produces a plain-text representation of the document.
 * It can be used for generating text-only versions of documents for accessibility or
 * for further processing by text-based tools.
 */
@Suppress("UnusedReceiverParameter")
fun RendererFactory.plainText(context: Context) =
    RenderingComponents(
        nodeRenderer = PlainTextNodeRenderer(context),
        postRenderer = PlainTextPostRenderer(context),
    )
