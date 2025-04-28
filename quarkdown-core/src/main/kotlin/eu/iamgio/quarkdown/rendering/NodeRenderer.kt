package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A rendering strategy, which converts nodes from the AST to their output code representation.
 */
interface NodeRenderer : NodeVisitor<CharSequence>
