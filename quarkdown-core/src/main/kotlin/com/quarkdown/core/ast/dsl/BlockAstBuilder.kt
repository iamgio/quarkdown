package com.quarkdown.core.ast.dsl

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.quarkdown.block.ImageFigure

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

    /**
     * @see Table
     * @see TableAstBuilder
     */
    fun table(block: TableAstBuilder.() -> Unit) = +Table(TableAstBuilder().apply(block).columns)

    /**
     * @see ImageFigure
     */
    fun figure(block: InlineAstBuilder.() -> Unit) = +ImageFigure(buildInline(block).single() as Image)
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
