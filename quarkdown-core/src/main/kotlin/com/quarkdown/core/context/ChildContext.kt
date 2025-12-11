package com.quarkdown.core.context

/**
 * A [Context] that has a parent context, forming a scope tree.
 * This context can access its parent's properties and inherit them.
 * @param C type of the parent context
 */
interface ChildContext<C : Context> : Context {
    /**
     * The parent context of this context in the scope tree.
     */
    val parent: C

    /**
     * @param predicate condition to match
     * @return the last context (upwards, towards the root, starting from this context) that matches the [predicate],
     *         or `null` if no parent in the scope tree matches the given condition
     */
    fun lastParentOrNull(predicate: (Context) -> Boolean): Context? =
        when {
            // This is the last context to match the condition.
            predicate(this) && !predicate(parent) -> this
            // The root context matches the condition.
            parent !is ChildContext<*> && predicate(parent) -> parent
            // Scan the parent context.
            else -> (parent as? ChildContext<*>)?.lastParentOrNull(predicate)
        }
}
