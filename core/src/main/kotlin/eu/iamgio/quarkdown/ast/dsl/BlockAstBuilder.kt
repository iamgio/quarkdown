package eu.iamgio.quarkdown.ast.dsl

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Heading
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
    fun root(block: BlockAstBuilder.() -> Unit) = +AstRoot(buildBlocks(block))

    /**
     * @see Paragraph
     */
    fun paragraph(block: InlineAstBuilder.() -> Unit) = +Paragraph(buildInline(block))

    /**
     * @see Heading
     */
    fun heading(
        level: Int,
        block: InlineAstBuilder.() -> Unit,
    ) = +Heading(level, buildInline(block))

    /**
     * @see BlockQuote
     */
    fun blockQuote(
        type: BlockQuote.Type? = null,
        attribution: (InlineAstBuilder.() -> Unit)? = null,
        block: BlockAstBuilder.() -> Unit,
    ) = +BlockQuote(
        type,
        attribution?.let(::buildInline),
        buildBlocks(block),
    )

    /**
     * @see OrderedList
     * @see ListAstBuilder
     */
    fun orderedList(
        startIndex: Int = 1,
        loose: Boolean,
        block: ListAstBuilder.() -> Unit,
    ) = +OrderedList(startIndex, loose, ListAstBuilder().apply(block).build())

    /**
     * @see UnorderedList
     * @see ListAstBuilder
     */
    fun unorderedList(
        loose: Boolean,
        block: ListAstBuilder.() -> Unit,
    ) = +UnorderedList(loose, ListAstBuilder().apply(block).build())
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
