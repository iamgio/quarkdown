package com.quarkdown.core.ast.iterator

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.util.flattenedChildren

/**
 * An iterator that performs a DFS traversal through the nodes of an AST,
 * allowing the registration of observers that will be notified when a node of a certain type is visited.
 */
class ObservableAstIterator : AstIterator {
    /**
     * Hooks that will be called when a node of a certain type is visited.
     */
    val hooks: MutableList<(Node) -> Unit> = mutableListOf()

    /**
     * Hooks that will be called when the traversal finishes.
     */
    private val onFinishedHooks: MutableList<() -> Unit> = mutableListOf()

    /**
     * Registers a hook that will be called when a node of type [T] is visited.
     * @param hook action to be called, with the visited node as parameter
     * @param T desired node type
     * @return this for concatenation
     */
    inline fun <reified T : Node> on(noinline hook: (T) -> Unit): ObservableAstIterator =
        apply {
            hooks.add {
                if (it is T) hook(it)
            }
        }

    /**
     * Registers a hook that will be called when the tree traversal fully finishes.
     */
    fun onFinished(hook: () -> Unit): ObservableAstIterator =
        apply {
            onFinishedHooks.add(hook)
        }

    /**
     * Collects the visited nodes of type [T] into a collection, as long as they satisfy a [condition].
     * @param condition condition to be satisfied for the node to be collected
     * @param T node type
     * @return an ordered list (DFS order) containing all the visited nodes of type [T] in the tree
     */
    inline fun <reified T : Node> collect(crossinline condition: (T) -> Boolean): List<T> =
        mutableListOf<T>().apply {
            on<T> {
                if (condition(it)) add(it)
            }
        }

    /**
     * Collects all the visited nodes of type [T] into a collection.
     * @param T node type
     * @return an ordered list (DFS order) containing all the visited nodes of type [T] in the tree
     */
    inline fun <reified T : Node> collectAll(): List<T> = collect { true }

    /**
     * Attaches a hook to this iterator.
     * @param hook hook to attach
     * @return this for concatenation
     * @see on
     */
    fun attach(hook: AstIteratorHook): ObservableAstIterator =
        apply {
            hook.attach(this)
        }

    override fun traverse(root: NestableNode) {
        root.flattenedChildren().forEach { node ->
            hooks.forEach { hook -> hook(node) }
        }

        onFinishedHooks.forEach { it() }
    }
}
