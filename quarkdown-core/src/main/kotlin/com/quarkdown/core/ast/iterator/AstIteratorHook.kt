package com.quarkdown.core.ast.iterator

/**
 * A hook that can be attached to an [ObservableAstIterator].
 */
interface AstIteratorHook {
    /**
     * Attaches this hook to the given [iterator].
     * @param iterator iterator to attach the hook to
     */
    fun attach(iterator: ObservableAstIterator)
}
