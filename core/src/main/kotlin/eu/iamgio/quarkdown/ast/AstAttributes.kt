package eu.iamgio.quarkdown.ast

/**
 * Additional information about the node tree, produced by the parsing stage.
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
 */
data class MutableAstAttributes(
    override val linkDefinitions: MutableList<LinkDefinition> = mutableListOf(),
) : AstAttributes

/**
 * @param reference reference link to lookup
 * @return the corresponding link node, if it exists
 */
fun AstAttributes.resolveLinkReference(reference: ReferenceLink): Link? {
    val definition = linkDefinitions.firstOrNull { it.label == reference.label } ?: return null
    // TODO make common interface to avoid this
    return Link(definition.label, definition.url, definition.title)
}
