package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.token.BlockTokenVisitor
import eu.iamgio.quarkdown.visitor.token.InlineTokenVisitor
import eu.iamgio.quarkdown.visitor.token.TokenVisitor
import eu.iamgio.quarkdown.visitor.token.TokenVisitorAdapter

/**
 * Provider of parser instances. Each factory method returns a specialized implementation for a specific kind of parsing.
 */
interface ParserFactory {
    /**
     * @param attributes writeable attributes that are modified during the parsing process,
     *                   and carry useful information for the next stages of the pipeline
     * @return a new [BlockTokenVisitor] instance that parses tokens into [Node]s.
     */
    fun newBlockParser(attributes: MutableAstAttributes): BlockTokenVisitor<Node>

    /**
     * @param attributes writeable attributes that are modified during the parsing process,
     *                   and carry useful information for the next stages of the pipeline
     * @return a new [BlockTokenVisitor] instance that parses tokens into [Node]s.
     */
    fun newInlineParser(attributes: MutableAstAttributes): InlineTokenVisitor<Node>

    /**
     * @param attributes writeable attributes that are modified during the parsing process,
     *                   and carry useful information for the next stages of the pipeline
     * @return a new [TokenVisitor] instance that includes operations by both [newBlockParser] and [newInlineParser]
     */
    fun newParser(attributes: MutableAstAttributes): TokenVisitor<Node> =
        TokenVisitorAdapter(newBlockParser(attributes), newInlineParser(attributes))
}
