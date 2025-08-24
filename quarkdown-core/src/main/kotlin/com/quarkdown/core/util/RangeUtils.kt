package com.quarkdown.core.util

/**
 * Returns a new [IntRange] with the specified [offset] added to both the start and end of the range.
 * @param offset the amount to offset the range by
 * @return a new [IntRange] with the offset applied
 */
fun IntRange.offset(offset: Int): IntRange = IntRange(first + offset, last + offset)
