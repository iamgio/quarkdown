package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor
import eu.iamgio.quarkdown.parser.visitor.InlineTokenVisitor
import eu.iamgio.quarkdown.parser.visitor.TokenVisitor
import eu.iamgio.quarkdown.parser.visitor.TokenVisitorAdapter

/**
 * Provider of parser instances. Each factory method returns a specialized implementation for a specific kind of parsing.
 */
interface ParserFactory {
    /**
     * @return a new [BlockTokenVisitor] instance that parses tokens into [Node]s.
     */
    fun newBlockParser(): BlockTokenVisitor<Node>

    /**
     * @return a new [BlockTokenVisitor] instance that parses tokens into [Node]s.
     */
    fun newInlineParser(): InlineTokenVisitor<Node>

    /**
     * @return a new [TokenVisitor] instance that includes operations by both [newBlockParser] and [newInlineParser]
     */
    fun newParser(): TokenVisitor<Node> = TokenVisitorAdapter(newBlockParser(), newInlineParser())
}
