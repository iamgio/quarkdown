package eu.iamgio.quarkdown.ast.iterator

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.util.flattenedChildren

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
     * Collects all the visited nodes of type [T] into a collection.
     * @param T node type
     * @return an ordered list (DFS order) containing all the visited nodes of type [T] in the tree
     */
    inline fun <reified T : Node> collectAll(): List<T> =
        mutableListOf<T>().apply {
            on<T>(::add)
        }

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

    override fun run(root: NestableNode) {
        root.flattenedChildren().forEach { node ->
            hooks.forEach { hook -> hook(node) }
        }

        onFinishedHooks.forEach { it() }
    }
}
