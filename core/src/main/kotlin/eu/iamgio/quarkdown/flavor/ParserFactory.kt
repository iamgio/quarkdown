package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor

/**
 * Provider of parser instances. Each factory method returns a specialized implementation for a specific kind of parsing.
 */
interface ParserFactory {
    /**
     * @return a new [BlockTokenVisitor] instance that parses tokens into [Node]s.
     */
    fun newBlockParser(): BlockTokenVisitor<Node>
}
