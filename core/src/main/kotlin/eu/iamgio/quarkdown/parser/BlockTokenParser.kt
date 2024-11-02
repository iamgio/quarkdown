package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.BlankNode
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.HorizontalRule
import eu.iamgio.quarkdown.ast.base.block.Html
import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.block.list.ListBlock
import eu.iamgio.quarkdown.ast.base.block.list.ListItem
import eu.iamgio.quarkdown.ast.base.block.list.ListItemVariant
import eu.iamgio.quarkdown.ast.base.block.list.OrderedList
import eu.iamgio.quarkdown.ast.base.block.list.TaskListItemVariant
import eu.iamgio.quarkdown.ast.base.block.list.UnorderedList
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure
import eu.iamgio.quarkdown.ast.quarkdown.block.Math
import eu.iamgio.quarkdown.ast.quarkdown.block.PageBreak
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.factory.ValueFactory
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.lexer.patterns.DELIMITED_TITLE_HELPER
import eu.iamgio.quarkdown.lexer.tokens.BlockCodeToken
import eu.iamgio.quarkdown.lexer.tokens.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.tokens.BlockTextToken
import eu.iamgio.quarkdown.lexer.tokens.FencesCodeToken
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken
import eu.iamgio.quarkdown.lexer.tokens.HeadingToken
import eu.iamgio.quarkdown.lexer.tokens.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.tokens.HtmlToken
import eu.iamgio.quarkdown.lexer.tokens.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.tokens.ListItemToken
import eu.iamgio.quarkdown.lexer.tokens.MultilineMathToken
import eu.iamgio.quarkdown.lexer.tokens.NewlineToken
import eu.iamgio.quarkdown.lexer.tokens.OnelineMathToken
import eu.iamgio.quarkdown.lexer.tokens.OrderedListToken
import eu.iamgio.quarkdown.lexer.tokens.PageBreakToken
import eu.iamgio.quarkdown.lexer.tokens.ParagraphToken
import eu.iamgio.quarkdown.lexer.tokens.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.tokens.TableToken
import eu.iamgio.quarkdown.lexer.tokens.UnorderedListToken
import eu.iamgio.quarkdown.lexer.walker.ARG_DELIMITER_CLOSE
import eu.iamgio.quarkdown.lexer.walker.ARG_DELIMITER_OPEN
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.nextOrNull
import eu.iamgio.quarkdown.util.removeOptionalPrefix
import eu.iamgio.quarkdown.util.takeUntilLastOccurrence
import eu.iamgio.quarkdown.util.trimDelimiters
import eu.iamgio.quarkdown.visitor.token.BlockTokenVisitor

/**
 * The position of this character in the delimiter of a table header defines its column alignment.
 */
private const val TABLE_ALIGNMENT_CHAR = ':'

/**
 * A parser for block tokens.
 * @param context additional data to fill during the parsing process
 */
class BlockTokenParser(private val context: MutableContext) : BlockTokenVisitor<Node> {
    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this.tokenize()
            .acceptAll(context.flavor.parserFactory.newParser(context))

    /**
     * @return [this] raw string tokenized and parsed into processed inline content,
     *                based on this [flavor]'s specifics
     */
    private fun String.toInline(): InlineContent =
        context.flavor.lexerFactory.newInlineLexer(this)
            .tokenizeAndParse()

    override fun visit(token: NewlineToken): Node {
        return Newline
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
        return HorizontalRule
    }

    /**
     * Splits a heading text and its custom ID from a raw text.
     * `Heading {#custom-id}` -> `Heading`, `custom-id`
     * @param text heading text
     * @return a pair of the heading text and its custom ID
     */
    private fun splitHeadingTextAndId(text: String): Pair<String, String?> {
        val customIdMatch = "\\s+\\{#([^}]+)}\$".toRegex().find(text)
        val customId = customIdMatch?.groupValues?.get(1) // {#custom-id} -> custom-id

        // Trim the custom ID from the text.
        val trimmedText = customIdMatch?.let { text.removeSuffix(it.value) } ?: text

        return trimmedText to customId
    }

    override fun visit(token: HeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)

        val depth = groups.next().length // Amount of # characters.

        val rawText = groups.next().trim().takeUntilLastOccurrence(" #") // Remove trailing # characters.
        // Heading {#custom-id} -> Heading, custom-id
        val (text, customId) = splitHeadingTextAndId(rawText)

        return Heading(
            depth,
            text.toInline(),
            customId,
        )
    }

    override fun visit(token: SetextHeadingToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)

        val rawText = groups.next().trim()
        // Heading {#custom-id} -> Heading, custom-id
        val (text, customId) = splitHeadingTextAndId(rawText)

        return Heading(
            text = text.toInline(),
            depth =
                when (groups.next().firstOrNull()) {
                    '=' -> 1
                    '-' -> 2
                    else -> throw IllegalStateException("Invalid setext heading characters") // Should not happen
                },
            customId = customId,
        )
    }

    override fun visit(token: LinkDefinitionToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)

        return LinkDefinition(
            label = groups.next().trim().toInline(),
            url = groups.next().trim(),
            // Remove first and last character
            title = groups.nextOrNull()?.trimDelimiters()?.trim(),
        )
    }

    /**
     * Parses list items from a list [token].
     * @param token list token to extract the items from
     */
    private fun extractListItems(token: Token) =
        context.flavor.lexerFactory.newListLexer(source = token.data.text)
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

    /**
     * Like [String.trimIndent], but each line requires at least [minIndent] whitespaces trimmed.
     */
    private fun trimMinIndent(
        lines: Sequence<String>,
        minIndent: Int,
    ): String {
        // Gets the amount of indentation to trim from the content.
        var indent = minIndent
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

        return trimmedContent
    }

    override fun visit(token: ListItemToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val marker = groups.next() // Bullet/number
        groups.next() // Consume
        val task = groups.next() // Optional GFM task

        val content = token.data.text.removePrefix(marker).removePrefix(task)
        val lines = content.lineSequence()

        if (lines.none()) {
            return ListItem(children = emptyList())
        }

        // Trims the content, removing common indentation.
        val trimmedContent = trimMinIndent(lines, minIndent = marker.trim().length)

        // Additional features of this list item.
        val variants =
            buildList<ListItemVariant> {
                // GFM 5.3 task list item.
                if (task.isNotBlank()) {
                    val isChecked = "[ ]" !in task
                    add(TaskListItemVariant(isChecked))
                }
            }

        // Parsed content.
        val children =
            context.flavor.lexerFactory
                .newBlockLexer(source = trimmedContent)
                .tokenizeAndParse()

        return ListItem(variants, children)
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
                .filter { it.isNotEmpty() }
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
            columns.getOrNull(index)?.alignment =
                when {
                    // :---:
                    delimiter.firstOrNull() == TABLE_ALIGNMENT_CHAR &&
                        delimiter.lastOrNull() == TABLE_ALIGNMENT_CHAR -> Table.Alignment.CENTER
                    // :---
                    delimiter.firstOrNull() == TABLE_ALIGNMENT_CHAR -> Table.Alignment.LEFT
                    // ---:
                    delimiter.lastOrNull() == TABLE_ALIGNMENT_CHAR -> Table.Alignment.RIGHT
                    // ---
                    else -> Table.Alignment.NONE
                }
        }

        // Quarkdown extension: a table may have a caption.
        // A caption is located at the end of the table, after a line break,
        // wrapped by a delimiter, the same way as a link/image title.
        // "This is a caption", 'This is a caption', (This is a caption)
        val captionRegex = Regex("^\\s*($DELIMITED_TITLE_HELPER)\\s*$")
        // The found caption of the table, if any.
        var caption: String? = null

        // Other rows.
        groups.next().lineSequence()
            .filterNot { it.isBlank() }
            .onEach { row ->
                // Extract the caption if this is the caption row.
                captionRegex.find(row)?.let { captionMatch ->
                    caption = captionMatch.groupValues.getOrNull(1)?.trimDelimiters()
                }
            }
            .filterNot { caption != null } // The caption row is at the end of the table and not part of the table itself.
            .forEach { row ->
                var cellCount = 0
                // Push cell.
                parseRow(row).forEachIndexed { index, cell ->
                    columns.getOrNull(index)?.cells?.add(cell)
                    cellCount = index
                }
                // Fill missing cells.
                for (remainingRow in cellCount + 1 until columns.size) {
                    columns[remainingRow].cells += Table.Cell(emptyList())
                }
            }

        return Table(
            columns = columns.map { Table.Column(it.alignment, it.header, it.cells) },
            caption,
        )
    }

    override fun visit(token: HtmlToken): Node {
        return Html(
            content = token.data.text.trim(),
        )
    }

    override fun visit(token: ParagraphToken): Node {
        val text = token.data.text.trim().toInline()

        // If the paragraph only consists of a single child, it could be a special block.
        return when (val singleChild = text.singleOrNull()) {
            // Single image -> a figure.
            is Image -> ImageFigure(singleChild)
            // Regular paragraph otherwise (most cases).
            else -> Paragraph(text)
        }
    }

    override fun visit(token: BlockQuoteToken): Node {
        // Remove leading >
        var text = token.data.text.replace("^ *>[ \\t]?".toRegex(RegexOption.MULTILINE), "").trim()

        // Blockquote type, if any. e.g. Tip, note, warning.
        val type: BlockQuote.Type? =
            BlockQuote.Type.entries.find { type ->
                val prefix = type.name + ": " // e.g. Tip:, Note:, Warning:
                // If the text begins with the prefix, it's a blockquote of that type.
                val (newText, prefixFound) = text.removeOptionalPrefix(prefix, ignoreCase = true)
                // If the prefix was found, it is stripped off.
                if (prefixFound) {
                    text = newText
                }

                // If the prefix was found, the type is set.
                prefixFound
            }

        // Content nodes.
        var children =
            context.flavor.lexerFactory
                .newBlockLexer(source = text)
                .tokenizeAndParse()

        // If the last child is a single-item unordered list, then it's not part of the blockquote,
        // but rather its content is the attribution of the citation.
        // Example:
        // > To be, or not to be, that is the question.
        // > - William Shakespeare
        val attribution: InlineContent? =
            (children.lastOrNull() as? UnorderedList)
                ?.children?.singleOrNull()?.let { it as? ListItem } // Only lists with one item are considered.
                ?.children?.firstOrNull()?.let { it as? TextNode } // Usually a paragraph.
                ?.text // The text of the attribution, as inline content.
                ?.also { children = children.dropLast(1) } // If found, the attribution is not part of the children.

        return BlockQuote(
            type,
            attribution,
            children,
        )
    }

    override fun visit(token: BlockTextToken): Node {
        return BlankNode
    }

    override fun visit(token: PageBreakToken): Node {
        return PageBreak()
    }

    override fun visit(token: FunctionCallToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)

        // Function name.
        val name = groups.next()

        // Function arguments.
        val arguments = mutableListOf<FunctionCallArgument>()

        // The name of the next argument. `null` means the next argument is unnamed.
        var argName: String? = null

        // Regular function arguments.
        groups.forEachRemaining { arg ->
            // If this group contains the name of a named argument,
            // it is applied to the very next argument.
            if (arg.firstOrNull() != ARG_DELIMITER_OPEN && arg.lastOrNull() != ARG_DELIMITER_CLOSE) {
                argName = arg
                return@forEachRemaining
            }

            // Regular argument wrapped in brackets, which are stripped off.
            // Common indentation is also removed.
            val argContent = arg.trimDelimiters().trimIndent().trim()

            // An expression from the raw string is created.
            ValueFactory.expression(argContent, context)?.let {
                arguments += FunctionCallArgument(it, argName)
                argName = null // The name of the next named argument is reset.
            }
        }

        // Body function argument.
        // A body argument is always the last one, it goes on a new line and each line is indented.
        token.data.namedGroups["bodyarg"]?.takeUnless { it.isBlank() }?.let { body ->
            // A body argument is treated as plain text, thus nested function calls are not executed by default.
            // They are executed if the argument is used as Markdown content from the referenced function,
            // that runs recursive lexing & parsing on the arg content, triggering function calls.

            // Remove indentation at the beginning of each line.
            val value = DynamicValue(body.trimIndent())

            arguments += FunctionCallArgument(value, isBody = true)
        }

        val call = FunctionCallNode(context, name, arguments, token.isBlock)

        // Enqueuing the function call, in order to expand it in the next stage.
        context.register(call)

        return call
    }
}
