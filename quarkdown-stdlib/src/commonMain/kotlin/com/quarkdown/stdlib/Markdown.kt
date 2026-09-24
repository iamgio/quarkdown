@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.block.Markdown
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Creates a block of raw Markdown content that is emitted verbatim only when the rendering target is Markdown.
 * Non-Markdown targets, such as HTML and plain text, discard the content.
 *
 * ```markdown
 * .markdown
 *     > This blockquote only appears in Markdown output.
 * ```
 *
 * @param content raw Markdown content to inject
 * @return a new [Markdown] node
 * @permission [Permission.NativeContent] to inject native Markdown content
 * @throws com.quarkdown.core.permissions.MissingPermissionException if [Permission.NativeContent] is not granted
 * @wiki markdown-content
 */
@QFunction
fun markdown(
    @Injected context: Context,
    @LikelyBody content: String,
): NodeValue {
    context.requirePermission(
        Permission.NativeContent,
        message = "Cannot inject native Markdown content",
    )
    return Markdown(content).wrappedAsValue()
}
