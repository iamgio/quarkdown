package eu.iamgio.quarkdown.util

/**
 * Filters null values out of a sequence of key-value pairs.
 * @return filtered sequence
 */
fun <A, B> Sequence<Pair<A, B?>>.filterValuesNotNull(): Sequence<Pair<A, B>> {
    @Suppress("UNCHECKED_CAST")
    return filterNot { it.second == null } as Sequence<Pair<A, B>>
}
