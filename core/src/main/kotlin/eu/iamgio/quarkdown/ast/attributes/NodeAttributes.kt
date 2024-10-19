package eu.iamgio.quarkdown.ast.attributes

/**
 *
 */
interface NodeAttributes {
    val location: List<Int>?
}

data class MutableNodeAttributes(
    override var location: List<Int>? = null,
) : NodeAttributes
