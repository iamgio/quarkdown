package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.LinkNode
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.library.Library

/**
 * An immutable [Context] implementation.
 * @param attributes attributes of the node tree, produced by the parsing stage
 * @param libraries loaded libraries to look up functions from
 */
open class BaseContext(
    private val attributes: AstAttributes,
    override val libraries: Set<Library> = emptySet(),
) : Context {
    override val hasMath: Boolean
        get() = attributes.hasMath

    override val functionCalls: List<FunctionCallNode>
        get() = attributes.functionCalls

    override fun getFunctionByName(name: String): Function<*>? {
        return libraries.asSequence()
            .flatMap { it.functions }
            .find { it.name == name }
    }

    override fun resolve(reference: ReferenceLink): LinkNode? {
        return attributes.linkDefinitions.firstOrNull { it.label == reference.reference }
            ?.let { Link(reference.label, it.url, it.title) }
    }
}
