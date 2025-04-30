package com.quarkdown.core.function.value.output.node

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.BlankNode
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.PairValue
import com.quarkdown.core.function.value.UnorderedCollectionValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * Producer of a [Node] output, ready to append to the AST, from a generic function output.
 * @see com.quarkdown.core.function.call.FunctionCallNodeExpander
 * @see BlockNodeOutputValueVisitor
 * @see InlineNodeOutputValueVisitor
 */
abstract class NodeOutputValueVisitor : OutputValueVisitor<Node> {
    private fun createListItems(value: IterableValue<*>): List<ListItem> =
        value.map {
            ListItem(
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

    // A pair is displayed as an ordered collection of its two elements.
    override fun visit(value: PairValue<*, *>): Node = visit(OrderedCollectionValue(value.unwrappedValue.toList()))

    // A dictionary is displayed as a key-value table.
    override fun visit(value: DictionaryValue<*>) =
        buildBlock {
            table {
                column({ text("Key") }) {
                    value.unwrappedValue.keys.forEach { key ->
                        cell { text(key) }
                    }
                }
                column({ text("Value") }) {
                    value.unwrappedValue.values.forEach { value ->
                        cell { +value.accept(this@NodeOutputValueVisitor) }
                    }
                }
            }
        }

    override fun visit(value: NodeValue) = value.unwrappedValue

    override fun visit(value: VoidValue) = BlankNode

    // Dynamic output (e.g. produced by the stdlib function `.function`) is treated:
    // - If it is a suitable output value: its content is visited again with this visitor.
    // - If it is a collection: its items are wrapped in a GeneralCollectionValue and visited.
    // - Otherwise: its string content is parsed as Markdown.
    @Suppress("UNCHECKED_CAST")
    override fun visit(value: DynamicValue): Node =
        when (value.unwrappedValue) {
            is OutputValue<*> -> value.unwrappedValue.accept(this)
            is Iterable<*> -> GeneralCollectionValue(value.unwrappedValue as Iterable<OutputValue<*>>).accept(this)
            is Node -> value.unwrappedValue
            else -> this.visit(parseRaw(value.unwrappedValue.toString()))
        }

    /**
     * When a [DynamicValue] cannot be converted to a [NodeValue], its string content is parsed as Markdown.
     * @param raw string content of the [DynamicValue]
     * @return wrapped node parsed from the raw Markdown string
     */
    protected abstract fun parseRaw(raw: String): NodeValue
}
