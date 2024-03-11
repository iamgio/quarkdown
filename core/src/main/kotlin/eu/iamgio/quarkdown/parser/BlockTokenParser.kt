@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.*
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.lexer.*
import eu.iamgio.quarkdown.parser.visitor.BlockTokenVisitor
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.takeUntilLastOccurrence

/**
 * A parser for block tokens.
 * @param flavor flavor to use in order to analyze and parse sub-blocks
 */
class BlockTokenParser(private val flavor: MarkdownFlavor) : BlockTokenVisitor<Node> {
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
        val groups = token.data.groups.iterator(consumeAmount = 4)
        return Code(
            language = groups.next().takeIf { it.isNotBlank() }?.trim(),
            text = groups.next().trim(),
        )
    }

    override fun visit(token: MultilineMathToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 3)
        return Math(text = groups.next().trim())
    }

    override fun visit(token: OnelineMathToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Math(text = groups.next().trim())
    }

    override fun visit(token: HorizontalRuleToken): Node {
        return HorizontalRule()
    }

    override fun visit(token: HeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Heading(
            depth = groups.next().length,
            text = groups.next().trim().takeUntilLastOccurrence(" #"),
        )
    }

    override fun visit(token: SetextHeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
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
        val groups = token.data.groups.iterator(consumeAmount = 2)
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
        flavor.lexerFactory.newListLexer(source = token.data.text)
            .tokenize()
            .acceptAll(flavor.parserFactory.newParser())
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
        val groups = token.data.groups.iterator(consumeAmount = 3)

        // e.g. "1."
        val marker = groups.next().trim()

        return OrderedList(
            startIndex = marker.dropLast(1).toIntOrNull() ?: 1,
            isLoose = children.any { it is Newline },
            children,
        )
    }

    override fun visit(token: ListItemToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
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
        val children =
            flavor.lexerFactory.newBlockLexer(source = trimmedContent)
                .tokenize()
                .acceptAll(flavor.parserFactory.newParser())

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
            children =
                flavor.lexerFactory.newBlockLexer(source = text)
                    .tokenize()
                    .acceptAll(flavor.parserFactory.newParser()),
        )
    }

    override fun visit(token: BlockTextToken): Node {
        return BlockText()
    }
}
