package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
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
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.parseAll

/**
 * A parser for block tokens.
 * @param lexer lexer to parse sub-blocks with
 */
class BlockTokenParser(private val lexer: Lexer) : BlockTokenVisitor<Node> {
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
        return Code(
            language = null,
            // Remove first indentation
            text = token.data.text.replace("^ {1,4}".toRegex(RegexOption.MULTILINE), "").trim(),
        )
    }

    override fun visit(token: FencesCodeToken): Node {
        val groups = groupsIterator(token, consumeAmount = 4)
        return Code(
            language = groups.next().takeIf { it.isNotBlank() }?.trim(),
            text = groups.next().trim(),
        )
    }

    override fun visit(token: HorizontalRuleToken): Node {
        return HorizontalRule()
    }

    override fun visit(token: HeadingToken): Node {
        val groups = groupsIterator(token, consumeAmount = 2)
        return Heading(
            depth = groups.next().length,
            text =
                groups.next().trim().let {
                    // Trim trailing #s preceeded by a space
                    val trailingIndex = it.lastIndexOf(" #")
                    if (trailingIndex >= 0) {
                        it.substring(0, trailingIndex)
                    } else {
                        it
                    }
                },
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
        return Html(
            content = token.data.text.trim(),
        )
    }

    override fun visit(token: ParagraphToken): Node {
        return Paragraph(
            text = token.data.text.trim(),
        )
    }

    override fun visit(token: BlockQuoteToken): Node {
        // Remove leading >
        val text = token.data.text.replace("^ *>[ \\t]?".toRegex(RegexOption.MULTILINE), "").trim()

        return BlockQuote(
            children = lexer.copyWith(source = text).tokenize().parseAll(this),
        )
    }

    override fun visit(token: BlockTextToken): Node {
        return BlockText()
    }
}
