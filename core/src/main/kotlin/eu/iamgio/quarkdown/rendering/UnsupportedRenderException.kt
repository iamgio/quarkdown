package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.Node

/**
 * An exception thrown when a [eu.iamgio.quarkdown.rendering.NodeRenderer] tries rendering a node which is unsupported by its flavor.
 * @param node node whose rendering was attempted
 */
class UnsupportedRenderException(node: Node) : UnsupportedOperationException("$node rendering is not supported by this flavor.")
