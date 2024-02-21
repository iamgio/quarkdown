package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.FencesCode
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
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
import eu.iamgio.quarkdown.lexer.Token

/**
 * A parser for block tokens.
 */
class BlockTokenParser : BlockTokenVisitor<Node> {
    /**
     * @param token token to extract the group iterator from
     * @param consumeAmount amount of initial groups to consume/skip (the first group is always the whole match)
     * @return a new group iterator for this token, sliced from the first [consumeAmount] items
     */
    private fun groupsIterator(
        token: Token,
        consumeAmount: Int = 1,
    ) = token.data.groups.iterator().apply {
        repeat(consumeAmount) {
            next()
        }
    }

    override fun visit(token: NewlineToken): Node {
        return Newline()
    }

    override fun visit(token: BlockCodeToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: FencesCodeToken): Node {
        val groups = groupsIterator(token, consumeAmount = 4)
        return FencesCode(
            lang = groups.next().takeIf { it.isNotBlank() }?.trim(),
            text = groups.next().trim(),
        )
    }

    override fun visit(token: HorizontalRuleToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: HeadingToken): Node {
        val groups = groupsIterator(token, consumeAmount = 2)
        return Heading(
            depth = groups.next().length,
            text = groups.next().trim(),
        )
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
        return Paragraph(
            text = token.data.text.trim(),
            children = emptyList(),
        )
    }

    override fun visit(token: BlockQuoteToken): Node {
        TODO("Not yet implemented")
    }

    override fun visit(token: BlockTextToken): Node {
        TODO("Not yet implemented")
    }
}
