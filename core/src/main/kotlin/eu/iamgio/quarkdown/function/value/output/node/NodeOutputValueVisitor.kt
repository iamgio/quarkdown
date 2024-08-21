package eu.iamgio.quarkdown.function.value.output.node

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.BlockText
import eu.iamgio.quarkdown.ast.base.block.ListItem
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.block.UnorderedList
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.UnorderedCollectionValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * Producer of a [Node] output, ready to append to the AST, from a generic function output.
 * @see eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
 * @see BlockNodeOutputValueVisitor
 * @see InlineNodeOutputValueVisitor
 */
abstract class NodeOutputValueVisitor : OutputValueVisitor<Node> {
    private fun createListItems(value: IterableValue<*>): List<ListItem> =
        value.map {
            BaseListItem(
                children =
                    listOf(
                        // Each item is represented by its own Node output.
                        it.accept(this),
                    ),
            )
        }

    override fun visit(value: OrderedCollectionValue<*>) =
        OrderedList(
            startIndex = 1,
            isLoose = false,
            children = createListItems(value),
        )

    override fun visit(value: UnorderedCollectionValue<*>) =
        UnorderedList(
            isLoose = false,
            children = createListItems(value),
        )

    // A general collection is just converted to a group of nodes.
    override fun visit(value: GeneralCollectionValue<*>) = MarkdownContent(children = value.map { it.accept(this) })

    override fun visit(value: NodeValue) = value.unwrappedValue

    override fun visit(value: VoidValue) = BlockText()

    // Dynamic output (e.g. produced by the stdlib function `.function`) is treated:
    // - If it is a suitable output value: its content is visited again with this visitor.
    // - If it is a collection: its items are wrapped in a GeneralCollectionValue and visited.
    // - Otherwise: its string content is parsed as Markdown.
    @Suppress("UNCHECKED_CAST")
    override fun visit(value: DynamicValue): Node {
        return when (value.unwrappedValue) {
            is OutputValue<*> -> value.unwrappedValue.accept(this)
            is Iterable<*> -> GeneralCollectionValue(value.unwrappedValue as Iterable<OutputValue<*>>).accept(this)
            else -> this.visit(parseRaw(value.unwrappedValue.toString()))
        }
    }

    /**
     * When a [DynamicValue] cannot be converted to a [NodeValue], its string content is parsed as Markdown.
     * @param raw string content of the [DynamicValue]
     * @return wrapped node parsed from the raw Markdown string
     */
    protected abstract fun parseRaw(raw: String): NodeValue
}
