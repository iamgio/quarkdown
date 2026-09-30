package com.quarkdown.core.parser

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.BlankNode
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.HorizontalRule
import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.ListItemVariant
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.TaskListItemVariant
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.quarkdown.block.ImageFigure
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.takeCustomId
import com.quarkdown.core.lexer.scan.takeDelimitedTitle
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.BlockTextToken
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.lexer.tokens.HeadingToken
import com.quarkdown.core.lexer.tokens.HorizontalRuleToken
import com.quarkdown.core.lexer.tokens.HtmlToken
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken
import com.quarkdown.core.lexer.tokens.ListItemToken
import com.quarkdown.core.lexer.tokens.MultilineMathToken
import com.quarkdown.core.lexer.tokens.NewlineToken
import com.quarkdown.core.lexer.tokens.OnelineMathToken
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.PageBreakToken
import com.quarkdown.core.lexer.tokens.ParagraphToken
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import com.quarkdown.core.lexer.tokens.TableToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken
import com.quarkdown.core.util.removeOptionalPrefix
import com.quarkdown.core.util.trimDelimiters
import com.quarkdown.core.visitor.token.BlockTokenVisitor

/**
 * The position of this character in the delimiter of a table header defines its column alignment.
 */
private const val TABLE_ALIGNMENT_CHAR = ':'

/**
 * A parser for block tokens.
 * @param context additional data to fill during the parsing process
 */
class BlockTokenParser(
    private val context: MutableContext,
) : BlockTokenVisitor<Node> {
    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this
            .tokenize()
            .acceptAll(context.flavor.parserFactory.newParser(context))
            .toList()

    /**
     * @return [this] raw string tokenized and parsed into processed inline content,
     *                based on this flavor's specifics
     */
    private fun String.toInline(): InlineContent =
        context.flavor.lexerFactory
            .newInlineLexer(this)
            .tokenizeAndParse()

    override fun visit(token: NewlineToken): Node = Newline

    override fun visit(token: BlockCodeToken): Node =
        Code(
            language = null,
            content = token.content.trim(),
        )

    override fun visit(token: FencesCodeToken): Node {
        // Removes, at most, the initial spaces from each line (GFM #101).
        val content =
            token.content
                .lineSequence()
                .joinToString(separator = "\n") { line -> line.drop(line.takeWhile { it == ' ' }.length.coerceAtMost(token.indent)) }
                .removePrefix("\n")
                .removeSuffix("\n")

        return Code(
            language = token.language?.takeIf { it.isNotBlank() }?.trim(),
            caption =
                token.caption
                    ?.trim()
                    ?.trimDelimiters()
                    ?.toInline(),
            referenceId = token.customId?.trim(),
            content = content,
        )
    }

    override fun visit(token: MultilineMathToken): Node =
        Math(
            expression = token.expression.trim(),
            referenceId = token.customId?.trim(),
        )

    override fun visit(token: OnelineMathToken): Node =
        Math(
            expression = token.expression.trim(),
            referenceId = token.customId?.trim(),
        )

    override fun visit(token: HorizontalRuleToken): Node = HorizontalRule

    override fun visit(token: HeadingToken): Node =
        Heading(
            token.depth,
            token.content.trim().toInline(),
            customId = token.customId?.trim(),
            canBreakPage = !token.isDecorative,
            canTrackLocation = !token.isDecorative,
            excludeFromTableOfContents = token.isDecorative,
        )

    override fun visit(token: SetextHeadingToken): Node =
        Heading(
            text = token.content.trim().toInline(),
            depth =
                when (token.underline.firstOrNull()) {
                    '=' -> 1
                    '-' -> 2
                    else -> throw IllegalStateException("Invalid setext heading characters") // Should not happen
                },
            customId = token.customId?.trim(),
        )

    override fun visit(token: LinkDefinitionToken): Node =
        LinkDefinition(
            label = token.label.trim().toInline(),
            url = token.url.trim(),
            // Remove first and last character
            title =
                token.title
                    ?.trimDelimiters()
                    ?.trim()
                    ?.toInline(),
            fileSystem = context.fileSystem,
        )

    override fun visit(token: FootnoteDefinitionToken): Node =
        FootnoteDefinition(
            label = token.label.trim(),
            text = token.content.trim().toInline(),
        )

    /**
     * Parses list items from a list [token].
     * @param token list token to extract the items from
     */
    private fun extractListItems(token: Token) =
        context.flavor.lexerFactory
            .newListLexer(source = token.data.text)
            .tokenizeAndParse()
            .dropLastWhile { it is Newline } // Remove trailing blank lines

    /**
     * Sets [list] as the owner of each of its [ListItem]s.
     * Ownership is used while rendering to determine whether a [ListItem]
     * is part of a loose or tight list.
     * @param list list to set ownership for
     */
    private fun updateListItemsOwnership(list: ListBlock) {
        list.children
            .asSequence()
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

        // e.g. "1."
        val marker = token.marker.trim()

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
        val marker = token.marker // Bullet/number
        val task = token.task // Optional GFM task

        val content =
            token.data.text
                .removePrefix(marker)
                .removePrefix(task)

        if (content.isBlank()) {
            return ListItem(children = emptyList(), rawContent = content)
        }

        val lines = content.lineSequence()

        // Trims the content, removing common indentation.
        val trimmedContent = trimMinIndent(lines, minIndent = marker.length).trimEnd()

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

        return ListItem(variants, children, rawContent = trimmedContent)
    }

    /**
     * A table's trailing metadata row.
     * @param caption raw caption including its delimiters, if any
     * @param customId raw cross-reference identifier, if any
     */
    private class TableMetadata(
        val caption: String?,
        val customId: String?,
    )

    /**
     * Quarkdown extension: a table may have metadata on its last row, a caption wrapped by a delimiter the
     * same way as a link or image title, and a custom ID for cross-referencing. The row holds nothing else.
     * @param row raw row to inspect
     * @return the metadata, or `null` when the row is an ordinary cell row
     */
    private fun tableMetadataOf(row: String): TableMetadata? =
        Scanner(row).run {
            takeSpacesAndTabs()
            val caption = takeDelimitedTitle()
            takeSpacesAndTabs()
            val customId = takeCustomId()
            takeSpacesAndTabs()
            if (isAtEnd && (caption != null || customId != null)) TableMetadata(caption, customId) else null
        }

    override fun visit(token: TableToken): Node {
        val columns = mutableListOf<Table.MutableColumn>()
        val separator = "|"

        /**
         * Extracts the cells from a table row as raw strings.
         */
        fun splitRow(row: String): Sequence<String> =
            row
                .trim()
                .removePrefix(separator)
                .removeSuffix(separator)
                .split(Regex("(?<!\\\\)" + Regex.escape(separator)))
                .asSequence()
                .map { it.replace("\\$separator", separator) } // Unescaping separators (#241)
                .map { it.trim() }

        /**
         * Extracts the cells from a table row as processed [Table.Cell]s.
         */
        fun parseRow(row: String): Sequence<Table.Cell> = splitRow(row).map { Table.Cell(it.toInline()) }

        // Header row.
        parseRow(token.header).forEach {
            columns += Table.MutableColumn(Table.Alignment.NONE, it, mutableListOf())
        }

        // Delimiter row (defines alignment).
        splitRow(token.alignment).forEachIndexed { index, delimiter ->
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

        // The found caption and custom ID (reference ID) of the table, if any.
        var metadataFound = false
        var caption: String? = null
        var customId: String? = null

        // Other rows.
        token.rows
            .lineSequence()
            .filterNot { it.isBlank() }
            .onEach { row ->
                // Extract the metadata if this is the metadata row.
                tableMetadataOf(row)?.let { metadata ->
                    metadataFound = true
                    caption = metadata.caption?.trimDelimiters()
                    customId = metadata.customId?.trim()
                }
            }.filterNot { metadataFound } // The metadata row is at the end of the table and not part of the table itself.
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
            columns = columns.map { it.toColumn() },
            caption = caption?.toInline(),
            referenceId = customId,
        )
    }

    override fun visit(token: HtmlToken): Node =
        Html(
            content = token.data.text.trim(),
        )

    override fun visit(token: ParagraphToken): Node {
        val text =
            token.data.text
                .trim()
                .toInline()

        // If the paragraph only consists of a single child, it could be a special block.
        return when (val singleChild = text.singleOrNull()) {
            // Single image -> a figure.
            is Image -> ImageFigure(singleChild)

            // Regular paragraph otherwise (most cases).
            else -> Paragraph(text)
        }
    }

    override fun visit(token: BlockQuoteToken): Node {
        var text = token.content.trim()

        // Blockquote type, if any. e.g. Tip, note, warning.
        val type: BlockQuote.Type? =
            BlockQuote.Type.entries.find { type ->
                sequenceOf(
                    "${type.name}: ", // e.g. Tip:, Note:, Warning:
                    "[!${type.name}]", // e.g. [!TIP], [!NOTE], [!WARNING]
                ).any { prefix ->
                    val (newText, found) = text.removeOptionalPrefix(prefix, ignoreCase = true)
                    if (found) text = newText.trimStart()
                    found
                }
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
                ?.children
                ?.singleOrNull()
                ?.let { it as? ListItem } // Only lists with one item are considered.
                ?.children
                ?.firstOrNull()
                ?.let { it as? TextNode } // Usually a paragraph.
                ?.text // The text of the attribution, as inline content.
                ?.also { children = children.dropLast(1) } // If found, the attribution is not part of the children.

        return BlockQuote(
            type,
            attribution,
            children,
        )
    }

    override fun visit(token: BlockTextToken): Node = BlankNode

    override fun visit(token: PageBreakToken): Node = PageBreak()

    override fun visit(token: FunctionCallToken): Node {
        val result = token.walkerResult
        val call = result.value

        // The range of the function call in the source code.
        // Note: the end index is provided by the walker, not the lexer.
        val sourceRangeStart = token.data.position.first
        val sourceRangeEnd = sourceRangeStart + result.endIndex
        val sourceRange = sourceRangeStart..sourceRangeEnd
        val sourceText = result.sourceText

        // The syntax-only information held by the walked function call is converted to a context-aware function call node.
        // Function chaining is also handled here, delegated to the refiner.
        val callNode = FunctionCallRefiner(context, call, token.isBlock, sourceText, sourceRange).toNode()

        // Enqueuing the function call, in order to expand it in the next stage of the pipeline.
        context.register(callNode)

        return callNode
    }
}
