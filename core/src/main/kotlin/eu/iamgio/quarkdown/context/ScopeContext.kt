package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.function.Function

/**
 * A context that is the result of a fork from an original parent [Context].
 * Several properties are inherited from it.
 * @param parent context this scope was forked from
 */
class ScopeContext(val parent: Context) : MutableContext(
    flavor = parent.flavor,
    errorHandler = parent.errorHandler,
    libraries = emptySet(),
) {
    /**
     * If no matching function is found among this [ScopeContext]'s own [libraries],
     * [parent]'s libraries are scanned.
     * @see Context.getFunctionByName
     */
    override fun getFunctionByName(name: String): Function<*>? = super.getFunctionByName(name) ?: parent.getFunctionByName(name)

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
            parent !is ScopeContext && predicate(parent) -> parent
            // Scan the parent context.
            else -> (parent as? ScopeContext)?.lastParentOrNull(predicate)
        }
}
