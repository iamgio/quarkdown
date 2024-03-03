package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

/**
 *
 */
interface ParserFactory {
    fun newBlockParser(): BlockTokenVisitor<Node>
}
