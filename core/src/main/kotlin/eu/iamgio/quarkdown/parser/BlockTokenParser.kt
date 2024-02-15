package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.common.BlockTokenVisitor
import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.BlockTextToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken

/**
 * A parser for block tokens.
 */
class BlockTokenParser : BlockTokenVisitor<Node> {
    override fun visit(token: NewlineToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: BlockCodeToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: FencesCodeToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: HorizontalRuleToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: HeadingToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: SetextHeadingToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: LinkDefinitionToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: ListItemToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: HtmlToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: ParagraphToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: BlockQuoteToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: BlockTextToken): Node {
        TODO("Not yet implemented")
    }
}
