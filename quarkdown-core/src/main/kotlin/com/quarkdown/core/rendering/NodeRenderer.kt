package com.quarkdown.core.rendering

import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A rendering strategy, which converts nodes from the AST to their output code representation.
 */
interface NodeRenderer : NodeVisitor<CharSequence>
