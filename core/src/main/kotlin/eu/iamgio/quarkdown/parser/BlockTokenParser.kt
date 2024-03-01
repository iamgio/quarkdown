package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.OrderedList
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.TaskListItem
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.ast.visitor.BlockTokenVisitor
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
import eu.iamgio.quarkdown.lexer.MathToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.OrderedListToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.UnorderedListToken
import eu.iamgio.quarkdown.lexer.impl.ListItemLexer
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

    override fun visit(token: MathToken): Node {
        val groups = groupsIterator(token, consumeAmount = 3)
        return Math(
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
        val groups = groupsIterator(token, consumeAmount = 2)
        return Heading(
            text = groups.next().trim(),
            depth =
                when (groups.next().firstOrNull()) {
                    '=' -> 1
                    '-' -> 2
                    else -> throw IllegalStateException("Invalid setext heading characters") // Should not happen
                },
        )
    }

    override fun visit(token: LinkDefinitionToken): Node {
        val groups = groupsIterator(token, consumeAmount = 2)
        return LinkDefinition(
            text = groups.next().trim(),
            url = groups.next().trim(),
            title =
                if (groups.hasNext()) {
                    // Remove first and last character
                    groups.next().trim().let { it.substring(1, it.length - 1) }.trim()
                } else {
                    null
                },
        )
    }

    /**
     * Parses list items from a list [token].
     * @param token list token to extract the items from
     */
    private fun extractListItems(token: Token) =
        ListItemLexer(source = token.data.text)
            .tokenize()
            .parseAll(this)
            .dropLastWhile { it is Newline } // Remove trailing blank lines

    override fun visit(token: UnorderedListToken): Node {
        val children = extractListItems(token)

        return UnorderedList(
            isLoose = children.any { it is Newline },
            children,
        )
    }

    override fun visit(token: OrderedListToken): Node {
        val children = extractListItems(token)
        val groups = groupsIterator(token, consumeAmount = 3)

        // e.g. "1."
        val marker = groups.next().trim()

        return OrderedList(
            startIndex = marker.dropLast(1).toIntOrNull() ?: 1,
            isLoose = children.any { it is Newline },
            children,
        )
    }

    override fun visit(token: ListItemToken): Node {
        val groups = groupsIterator(token, consumeAmount = 2)
        val marker = groups.next() // Bullet/number
        groups.next() // Consume
        val task = groups.next() // Optional GFM task

        val content = token.data.text.removePrefix(marker).removePrefix(task)
        val lines = content.lineSequence()

        if (lines.none()) {
            return BaseListItem(children = emptyList())
        }

        // Gets the amount of indentation to trim from the content.
        var indent = marker.trim().length
        for (char in lines.first()) {
            if (char.isWhitespace()) {
                indent++
            } else {
                break
            }
        }

        // Removes indentation from each line.
        val trimmedContent =
            lines.joinToString(separator = "\n") {
                it.replaceFirst("^ {1,$indent}".toRegex(), "")
            }

        // Parsed content.
        val children = lexer.copyWith(source = trimmedContent).tokenize().parseAll(this)

        return when {
            // GFM task list item.
            task.isNotBlank() -> {
                val isChecked = "[ ]" !in task
                TaskListItem(isChecked, children)
            }
            // Regular list item.
            else -> BaseListItem(children)
        }
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
