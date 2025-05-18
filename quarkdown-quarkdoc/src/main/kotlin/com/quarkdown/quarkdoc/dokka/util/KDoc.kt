package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.WithChildren
import org.jetbrains.dokka.model.withDescendants

/**
 * Finds the first child of type [T] in the documentation tree, starting from [this] root.
 * @param predicate optional predicate to filter the children of type [T]
 * @return the first child of type [T] that matches the predicate, if any
 */
inline fun <reified T> WithChildren<*>.findDeep(crossinline predicate: (T) -> Boolean = { true }): T? =
    withDescendants().firstOrNull { it is T && predicate(it) } as? T
