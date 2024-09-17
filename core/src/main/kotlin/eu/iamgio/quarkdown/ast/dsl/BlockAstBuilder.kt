package eu.iamgio.quarkdown.ast.dsl

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.UnorderedList

/**
 * A builder of block nodes.
 */
class BlockAstBuilder : AstBuilder() {
    /**
     * @see AstRoot
     */
    fun root(block: BlockAstBuilder.() -> Unit) = node(AstRoot(buildBlocks(block)))

    /**
     * @see Paragraph
     */
    fun paragraph(block: InlineAstBuilder.() -> Unit) = node(Paragraph(buildInline(block)))

    /**
     * @see BlockQuote
     */
    fun blockQuote(block: BlockAstBuilder.() -> Unit) = node(BlockQuote(children = buildBlocks(block)))

    /**
     * @see OrderedList
     * @see ListAstBuilder
     */
    fun orderedList(
        startIndex: Int = 1,
        loose: Boolean,
        block: ListAstBuilder.() -> Unit,
    ) = node(OrderedList(startIndex, loose, ListAstBuilder().apply(block).build()))

    /**
     * @see UnorderedList
     * @see ListAstBuilder
     */
    fun unorderedList(
        loose: Boolean,
        block: ListAstBuilder.() -> Unit,
    ) = node(UnorderedList(loose, ListAstBuilder().apply(block).build()))
}

/**
 * Begins a DSL block for building block nodes.
 * @param block action to run with the block builder
 * @return the built nodes
 * @see BlockAstBuilder
 */
fun buildBlocks(block: BlockAstBuilder.() -> Unit): List<Node> {
    return BlockAstBuilder().apply(block).build()
}

/**
 * Begins a DSL block for building a single block node.
 * @param block action to run with the block builder
 * @return the first node that results from [buildBlocks]
 * @throws IllegalStateException if the result of [buildBlocks] is empty
 * @see BlockAstBuilder
 */
fun buildBlock(block: BlockAstBuilder.() -> Unit): Node {
    return buildBlocks(block).firstOrNull() ?: throw IllegalStateException("buildBlock requires at least one node")
}
