package com.quarkdown.processor.util

/**
 * Wraps this identifier in backticks unconditionally. Backticks are legal on any Kotlin
 * identifier, so the generator always applies them to identifiers it emits - simpler than
 * tracking which subset of names would collide with hard keywords.
 */
internal fun String.backtick(): String = "`$this`"

/** Backticks only the last dotted segment of a fully-qualified name (`foo.bar.if` -> `` foo.bar.`if` ``). */
internal fun String.backtickLastSegment(): String {
    val idx = lastIndexOf('.')
    return if (idx < 0) backtick() else substring(0, idx + 1) + substring(idx + 1).backtick()
}
