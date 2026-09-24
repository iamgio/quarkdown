package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent

/**
 * When a [Heading] has this depth value, it is considered an invisible referenceable mark.
 * Depth 0 cannot be achieved with plain Markdown, but it can be supplied by a Quarkdown function.
 */
private const val MARKER_HEADING_DEPTH = 0

/**
 * Whether this heading is a marker.
 * @see marker
 */
val Heading.isMarker: Boolean
    get() = depth == MARKER_HEADING_DEPTH

/**
 * Creates an invisible [Heading] that acts as a marker that can be referenced by other elements in the document.
 * A useful use case would be, for example, in combination with a [com.quarkdown.core.context.toc.TableOfContents].
 * Depth 0 cannot be achieved with plain Markdown, but it can be supplied by the Quarkdown function `.marker`.
 */
fun Heading.Companion.marker(name: InlineContent) = Heading(MARKER_HEADING_DEPTH, name, canBreakPage = false)
