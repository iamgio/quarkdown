package com.quarkdown.core.ast.attributes.id

import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.property.Property

/**
 * [Property] that stores the 0-based occurrence index of a [Heading]'s base identifier within the document.
 *
 * - `0` for the first heading that produces a given base identifier;
 * - `1` for the second heading that would collide with the first;
 * - `2` for the third, and so on.
 *
 * Renderers can use this index to disambiguate colliding heading identifiers (e.g. by appending `-2`, `-3`, ...).
 *
 * Populated by [com.quarkdown.core.context.hooks.HeadingIdentifierDeduplicationHook] during tree traversal,
 * so that potentially parallel rendering reads from a fully-populated, deterministic state.
 *
 * @param value the deduplication index
 */
data class HeadingIdentifierDeduplicationIndexProperty(
    override val value: Int,
) : Property<Int> {
    companion object : Property.Key<Int>

    override val key = HeadingIdentifierDeduplicationIndexProperty
}

/**
 * @param context context where the deduplication index is stored
 * @return the deduplication index of this heading's base identifier within the document,
 *         or `0` if no index is registered (i.e. unique within the document)
 */
fun Heading.getIdentifierDeduplicationIndex(context: Context): Int =
    context.attributes.of(this)[HeadingIdentifierDeduplicationIndexProperty] ?: 0

/**
 * Registers the deduplication index of this heading's base identifier within the document.
 * @param context context where the index is stored
 * @param index 0-based occurrence index, where `0` means the first occurrence
 */
fun Heading.setIdentifierDeduplicationIndex(
    context: MutableContext,
    index: Int,
) {
    context.attributes.of(this) += HeadingIdentifierDeduplicationIndexProperty(index)
}
