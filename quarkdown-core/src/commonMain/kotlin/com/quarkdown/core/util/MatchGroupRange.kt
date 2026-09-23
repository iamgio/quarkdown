package com.quarkdown.core.util

/**
 * The range of indices this group matched in the input, which every platform exposes
 * but the common API does not.
 */
internal expect val MatchGroup.matchRange: IntRange
