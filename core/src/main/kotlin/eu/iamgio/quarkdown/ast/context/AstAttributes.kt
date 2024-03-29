package eu.iamgio.quarkdown.ast.context

import eu.iamgio.quarkdown.ast.LinkDefinition

/**
 * Additional information about the node tree, produced by the parsing stage and stored in a [Context].
 * @see Context
 */
interface AstAttributes {
    /**
     * The defined links, which can be referenced by other nodes.
     */
    val linkDefinitions: List<LinkDefinition>
}

/**
 * Writeable attributes that are modified during the parsing process,
 * and carry useful information for the next stages of the pipeline.
 * Storing these attributes while parsing prevents a further visit of the final tree.
 * @param linkDefinitions the defined links, which can be referenced by other nodes
 * @see MutableContext
 */
data class MutableAstAttributes(
    override val linkDefinitions: MutableList<LinkDefinition> = mutableListOf(),
) : AstAttributes
