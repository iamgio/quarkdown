package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode

/**
 * Additional information about the node tree, produced by the parsing stage and stored in a [eu.iamgio.quarkdown.context.Context].
 * @see eu.iamgio.quarkdown.context.Context
 */
interface AstAttributes {
    /**
     * The root node of the tree.
     */
    val root: NestableNode?

    /**
     * The defined links, which can be referenced by other nodes.
     */
    val linkDefinitions: List<LinkDefinition>

    /**
     * The function calls to be later executed.
     */
    val functionCalls: List<FunctionCallNode>

    /**
     * Whether there is at least one code block.
     * This is used to load the HighlightJS library in HTML rendering.
     */
    val hasCode: Boolean

    /**
     * Whether there is at least one math block or inline.
     * This is used to load the MathJax library in HTML rendering.
     */
    val hasMath: Boolean

    /**
     * @return a new copied mutable instance of these attributes
     */
    fun toMutable(): MutableAstAttributes
}

/**
 * Writeable attributes that are modified during the parsing process,
 * and carry useful information for the next stages of the pipeline.
 * Storing these attributes while parsing prevents a further visit of the final tree.
 * @param root the root node of the tree. According to the architecture, this is set right after the parsing stage
 * @param linkDefinitions the defined links, which can be referenced by other nodes
 * @param functionCalls the function calls to be later executed
 * @param hasCode whether there is at least one code block.
 * @param hasMath whether there is at least one math block or inline.
 * @see eu.iamgio.quarkdown.context.MutableContext
 */
data class MutableAstAttributes(
    override var root: NestableNode? = null,
    override val linkDefinitions: MutableList<LinkDefinition> = mutableListOf(),
    override val functionCalls: MutableList<FunctionCallNode> = mutableListOf(),
    override var hasCode: Boolean = false,
    override var hasMath: Boolean = false,
) : AstAttributes {
    override fun toMutable(): MutableAstAttributes = this.copy()
}
