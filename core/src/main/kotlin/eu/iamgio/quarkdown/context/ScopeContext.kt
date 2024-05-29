package eu.iamgio.quarkdown.context

/**
 *
 */
class ScopeContext(val parent: Context) : MutableContext(
    flavor = parent.flavor,
    errorHandler = parent.errorHandler,
    libraries = parent.libraries,
) {
    val depth: Int
        get() = ((parent as? ScopeContext)?.depth ?: 0) + 1

    val root: Context
        get() = (parent as? ScopeContext)?.root ?: parent

    /**
     * @return the last context (including this one) that matches the [predicate],
     *         or `null` if no parent in the scope tree matches the given condition
     */
    fun lastParentOrNull(predicate: (Context) -> Boolean): Context? =
        when {
            predicate(this) && !predicate(parent) -> this
            parent !is ScopeContext && predicate(parent) -> parent
            else -> (parent as? ScopeContext)?.lastParentOrNull(predicate)
        }
}

val Context.depth: Int
    get() =
        when (this) {
            is ScopeContext -> depth
            else -> 0
        }
