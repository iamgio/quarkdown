package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.BlockQuote
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Code
import eu.iamgio.quarkdown.ast.Heading
import eu.iamgio.quarkdown.ast.HorizontalRule
import eu.iamgio.quarkdown.ast.Html
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.LinkDefinition
import eu.iamgio.quarkdown.ast.ListBlock
import eu.iamgio.quarkdown.ast.ListItem
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.Newline
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.OrderedList
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.TaskListItem
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.ast.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
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
import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.OrderedListToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.TableToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.UnorderedListToken
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.nextOrNull
import eu.iamgio.quarkdown.util.takeUntilLastOccurrence
import eu.iamgio.quarkdown.util.trimDelimiters
import eu.iamgio.quarkdown.visitor.token.BlockTokenVisitor

/**
 * A parser for block tokens.
 * @param flavor flavor to use in order to analyze and parse sub-blocks
 * @param context additional data to fill during the parsing process
 */
class BlockTokenParser(
    private val flavor: MarkdownFlavor,
    private val context: MutableContext,
) : BlockTokenVisitor<Node> {
    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this.tokenize()
            .acceptAll(flavor.parserFactory.newParser(context))

    /**
     * @return [this] raw string tokenized and parsed into processed inline content,
     *                based on this [flavor]'s specifics
     */
    private fun String.toInline(): InlineContent =
        flavor.lexerFactory.newInlineLexer(this)
            .tokenizeAndParse()

    override fun visit(token: NewlineToken): Node {
        return Newline()
    }

    override fun visit(token: BlockCodeToken): Node {
        return Code(
            language = null,
            // Remove first indentation
            content = token.data.text.replace("^ {1,4}".toRegex(RegexOption.MULTILINE), "").trim(),
        )
    }

    override fun visit(token: FencesCodeToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 4)
        return Code(
            language = groups.next().takeIf { it.isNotBlank() }?.trim(),
            content = groups.next().trim(),
        )
    }

    override fun visit(token: MultilineMathToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 3)
        return Math(expression = groups.next().trim())
    }

    override fun visit(token: OnelineMathToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Math(expression = groups.next().trim())
    }

    override fun visit(token: HorizontalRuleToken): Node {
        return HorizontalRule()
    }

    override fun visit(token: HeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Heading(
            depth = groups.next().length,
            text = groups.next().trim().takeUntilLastOccurrence(" #").toInline(),
        )
    }

    override fun visit(token: SetextHeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Heading(
            text = groups.next().trim().toInline(),
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
        val definition =
            LinkDefinition(
                label = groups.next().trim().toInline(),
                url = groups.next().trim(),
                // Remove first and last character
                title = groups.nextOrNull()?.trimDelimiters()?.trim(),
            )

        // Storing the link definitions for easier lookups.
        context.register(definition)

        return definition
    }

    /**
     * Parses list items from a list [token].
     * @param token list token to extract the items from
     */
    private fun extractListItems(token: Token) =
        flavor.lexerFactory.newListLexer(source = token.data.text)
            .tokenizeAndParse()
            .dropLastWhile { it is Newline } // Remove trailing blank lines

    /**
     * Sets [list] as the owner of each of its [ListItem]s.
     * Ownership is used while rendering to determine whether a [ListItem]
     * is part of a loose or tight list.
     * @param list list to set ownership for
     */
    private fun updateListItemsOwnership(list: ListBlock) {
        list.children.asSequence()
            .filterIsInstance<ListItem>()
            .forEach { it.owner = list }
    }

    override fun visit(token: UnorderedListToken): Node {
        val children = extractListItems(token)

        return UnorderedList(
            isLoose = children.any { it is Newline },
            children,
        ).also(::updateListItemsOwnership)
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
        ).also(::updateListItemsOwnership)
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
        val children = flavor.lexerFactory.newBlockLexer(source = trimmedContent).tokenizeAndParse()

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

    override fun visit(token: TableToken): Node {
        /**
         * A temporary mutable [Table.Column].
         */
        class MutableColumn(var alignment: Table.Alignment, val header: Table.Cell, val cells: MutableList<Table.Cell>)

        val groups = token.data.groups.iterator(consumeAmount = 2)
        val columns = mutableListOf<MutableColumn>()

        /**
         * Extracts the cells from a table row as raw strings.
         */
        fun splitRow(row: String): Sequence<String> =
            row.split("(?<!\\\\)\\|".toRegex()).asSequence()
                .filterNot { it.isBlank() }
                .map { it.trim() }

        /**
         * Extracts the cells from a table row as processed [Table.Cell]s.
         */
        fun parseRow(row: String): Sequence<Table.Cell> = splitRow(row).map { Table.Cell(it.toInline()) }

        // Header row.
        parseRow(groups.next()).forEach {
            columns += MutableColumn(Table.Alignment.NONE, it, mutableListOf())
        }

        // Delimiter row (defines alignment).
        splitRow(groups.next()).forEachIndexed { index, delimiter ->
            // The position of this character in the delimiter defines the column alignment.
            val alignmentChar = ':'

            columns.getOrNull(index)?.alignment =
                when {
                    // :---:
                    delimiter.firstOrNull() == alignmentChar && delimiter.lastOrNull() == alignmentChar -> Table.Alignment.CENTER
                    // :---
                    delimiter.firstOrNull() == alignmentChar -> Table.Alignment.LEFT
                    // ---:
                    delimiter.lastOrNull() == alignmentChar -> Table.Alignment.RIGHT
                    // ---
                    else -> Table.Alignment.NONE
                }
        }

        // Other rows.
        groups.next().lineSequence()
            .filterNot { it.isBlank() }
            .forEach { row ->
                var cellCount = 0
                // Push cell.
                parseRow(row).forEachIndexed { index, cell ->
                    columns.getOrNull(index)?.cells?.add(cell)
                    cellCount = index
                }
                // Fill missing cells.
                ((cellCount + 1) until columns.size).forEach {
                    columns[it].cells += Table.Cell(emptyList())
                }
            }

        return Table(
            columns = columns.map { Table.Column(it.alignment, it.header, it.cells) },
        )
    }

    override fun visit(token: HtmlToken): Node {
        return Html(
            content = token.data.text.trim(),
        )
    }

    override fun visit(token: ParagraphToken): Node {
        return Paragraph(
            text = token.data.text.trim().toInline(),
        )
    }

    override fun visit(token: BlockQuoteToken): Node {
        // Remove leading >
        val text = token.data.text.replace("^ *>[ \\t]?".toRegex(RegexOption.MULTILINE), "").trim()

        return BlockQuote(
            children = flavor.lexerFactory.newBlockLexer(source = text).tokenizeAndParse(),
        )
    }

    override fun visit(token: BlockTextToken): Node {
        return BlockText()
    }
}
