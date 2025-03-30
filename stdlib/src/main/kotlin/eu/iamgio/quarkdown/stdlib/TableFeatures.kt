package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `TableFeatures` stdlib module exporter.
 * This module provides advanced functionality for tables, enhancing their capabilities
 * beyond basic data representation. It adds dynamic operations like sorting, filtering,
 * calculations, and conditional styling.
 */
val TableFeatures: Module =
    setOf(
        ::tableSort,
        ::tableFilter,
        ::tableCompute,
        ::tableStyle,
    )

enum class TableSortOrder {
    ASCENDING,
    DESCENDING,
    ;

    fun <T, R : Comparable<R>> apply(
        sequence: Sequence<T>,
        by: (T) -> R,
    ): Sequence<T> =
        when (this) {
            ASCENDING -> sequence.sortedBy(by)
            DESCENDING -> sequence.sortedByDescending(by)
        }
}

/**
 * Retrieves a specific column from a table.
 * @param content table to extract the column from
 * @param column index of the column (starting from 1)
 * @return a triple containing the table, the column, and the cells of the column, in order, as strings
 * @throws IllegalArgumentException if the supplied content is not a table or if the column index is out of bounds
 */
private fun getTableColumn(
    content: MarkdownContent,
    columnIndex: Int,
): Triple<Table, Table.Column, List<String>> {
    val table =
        content.children.firstOrNull() as? Table
            ?: throw IllegalArgumentException("Invalid argument: a table is required")

    // Index starts from 1.
    val normalizedColumnIndex = columnIndex - 1

    val column =
        table.columns.getOrNull(normalizedColumnIndex)
            ?: throw IllegalArgumentException("Column index must be between 1 and ${table.columns.size}")

    val values = column.cells.map { it.text.toPlainText() }

    return Triple(table, column, values)
}

/**
 * Edits a table by replacing its columns with the specified ones.
 * @param table the original table
 * @param columns the new columns to replace the original ones
 * @return a new table with the specified columns, and the same other properties as the original
 */
private fun editTable(
    table: Table,
    columns: List<Table.Column>,
) = Table(columns, table.caption)

/**
 * Reconstructs a table based on the specified row indexes.
 * @param table the original table
 * @param orderedRowIndexes the list of ordered row indexes
 * @return a new table with the same content as [table],
 * with the rows rearranged or filtered according to the new indexes
 */
private fun reconstructTable(
    table: Table,
    orderedRowIndexes: List<Int>,
): Table {
    val newColumns =
        table.columns.map {
            it.copy(cells = orderedRowIndexes.map(it.cells::get))
        }

    return editTable(table, newColumns)
}

/**
 * Sorts a table based on the values of a column.
 *
 * This function takes a table and returns a new table with rows sorted according to
 * the values in the specified column. Both text and numeric sorting are supported.
 *
 * Example:
 * ```
 * | Name | Age | City |
 * |------|-----|------|
 * | John | 25  | NY   |
 * | Lisa | 32  | LA   |
 * | Mike | 19  | CHI  |
 *
 * .tablesort {table} {2} {true}  // Sort by age (column 2) in descending order
 *
 * // Result:
 * | Name | Age | City |
 * |------|-----|------|
 * | Lisa | 32  | LA   |
 * | John | 25  | NY   |
 * | Mike | 19  | CHI  |
 * ```
 *
 * @param column index of the column (starting from 1)
 * @param order sorting order (`ascending` or `descending`)
 * @param content table to sort
 * @return the sorted table
 */
@Name("tablesort")
fun tableSort(
    @Name("column") columnIndex: Int,
    order: TableSortOrder = TableSortOrder.ASCENDING,
    @Name("table") content: MarkdownContent,
): NodeValue {
    val (table, _, values) = getTableColumn(content, columnIndex)

    // Obtain the indexes of the rows sorted by the reference column.
    val orderedRowIndexes: List<Int> =
        values
            .asSequence()
            .withIndex()
            .let { order.apply(it) { item -> item.value } }
            .map { it.index }
            .toList()

    return reconstructTable(table, orderedRowIndexes).wrappedAsValue()
}

/**
 * Filters the rows of a table based on a conditional expression.
 *
 * Only the rows that satisfy the condition for the specified column are kept in the resulting table.
 * The filtering is applied on the text content of the cells, using various operators for comparison.
 *
 * Supports various filter operators:
 * - contains:text - Cells containing the specified text
 * - >n, <n, =n - Numeric comparisons
 * - date:>date, date:<date - Date comparisons
 * - regex:pattern - Filtering via regular expression
 *
 * Example:
 * ```
 * | Product | Price | Category |
 * |---------|-------|----------|
 * | Laptop  | 1200  | Tech     |
 * | Chair   | 150   | Home     |
 * | Phone   | 800   | Tech     |
 * | Table   | 350   | Home     |
 *
 * .tablefilter {table} {2} {">300"}  // Filter to only show items costing more than 300
 *
 * // Result:
 * | Product | Price | Category |
 * |---------|-------|----------|
 * | Laptop  | 1200  | Tech     |
 * | Table   | 350   | Home     |
 * ```
 *
 * Additional filter examples:
 * ```
 * .tablefilter {table} {3} {contains:Tech}  // Products in Tech category
 * .tablefilter {table} {1} {regex:^T.*}     // Products starting with 'T'
 * ```
 *
 * @param content table to filter
 * @param columnIndex index of the column (1-based)
 * @param filterExpression filter expression
 * @return the filtered table
 */
@Name("tablefilter")
fun tableFilter(
    @Name("column") columnIndex: Int,
    filter: Lambda,
    @Name("table") content: MarkdownContent,
): NodeValue {
    val (table, _, values) = getTableColumn(content, columnIndex)

    val filteredRowIndexes =
        values
            .withIndex()
            .filter { item -> filter.invoke<Boolean, BooleanValue>(DynamicValue(item.value)).unwrappedValue }
            .map { it.index }

    return reconstructTable(table, filteredRowIndexes).wrappedAsValue()
}

/**
 * Performs calculations on the values of a column of a table.
 *
 * This function adds a new row at the bottom of the table with the result of the
 * calculation applied to the numeric values in the specified column. Non-numeric
 * values are treated as 0.
 *
 * Supports the following aggregation functions:
 * - SUM - Sum of values
 * - AVG - Average of values
 * - COUNT - Count of elements
 * - MIN - Minimum value
 * - MAX - Maximum value
 *
 * Example:
 * ```
 * | Item     | Quantity | Price | Total |
 * |----------|----------|-------|-------|
 * | Product A| 2        | 10    | 20    |
 * | Product B| 1        | 15    | 15    |
 * | Product C| 3        | 5     | 15    |
 *
 * .tablecompute {table} {SUM} {4}  // Calculate sum of Total column
 *
 * // Result:
 * | Item     | Quantity | Price | Total |
 * |----------|----------|-------|-------|
 * | Product A| 2        | 10    | 20    |
 * | Product B| 1        | 15    | 15    |
 * | Product C| 3        | 5     | 15    |
 * | SUM      |          |       | 50    |
 * ```
 *
 * Calculating average:
 * ```
 * .tablecompute {table} {AVG} {4}  // Result will add row with: AVG | | | 16.67
 * ```
 *
 * @param table table to compute on
 * @param formula aggregation function to apply
 * @param columnIndex index of the column (1-based)
 * @return the table with the result row added
 */
@Name("tablecompute")
fun tableCompute(
    @Name("column") columnIndex: Int,
    compute: Lambda,
    @Name("table") content: MarkdownContent,
): NodeValue {
    val (table, column, values) = getTableColumn(content, columnIndex)

    // `compute` is called with the collection of cell values as an argument.
    val cellValuesCollection = OrderedCollectionValue(values.map(::DynamicValue))
    val computedCell = compute.invokeDynamic(cellValuesCollection).unwrappedValue

    // Append the computed cell to the column, and empty cells to the others.
    val newColumns =
        table.columns.map {
            val resultCell =
                Table.Cell(
                    buildInline { if (it === column) text(computedCell.toString()) },
                )
            it.copy(cells = it.cells + resultCell)
        }

    return editTable(table, newColumns).wrappedAsValue()
}

/**
 * Applies conditional styles to the cells of a table.
 *
 * This function allows applying different HTML-based styles to table cells that
 * match a condition. The condition is evaluated on the specified column, but
 * the style is applied to all cells in the rows that match.
 *
 * Supports various styles:
 * - background:color - Background of the cell
 * - color:color - Text color
 * - bold, italic, underline, strike - Text formatting
 * - align:value - Text alignment
 * - custom CSS styles can be passed directly
 *
 * Example:
 * ```
 * | Name  | Score | Status    |
 * |-------|-------|-----------|
 * | Alice | 85    | Pass      |
 * | Bob   | 45    | Fail      |
 * | Carol | 93    | Pass      |
 * | Dave  | 67    | Pass      |
 *
 * .tablestyle {table} {2} {"<60"} {background:red}  // Highlight failing scores
 *
 * // Result: Bob's row will have red background
 * ```
 *
 * Multiple style examples:
 * ```
 * .tablestyle {table} {2} {">90"} {bold}                   // Bold for high scores
 * .tablestyle {table} {3} {contains:Fail} {color:red}      // Red text for failing status
 * .tablestyle {table} {2} {"<50"} {background:red;color:white} // Custom styling
 * ```
 *
 * @param table table to style
 * @param columnIndex index of the column for the condition (1-based)
 * @param condition conditional expression
 * @param style style to apply
 * @return the table with the styles applied
 */
@Name("tablestyle")
fun tableStyle(
    table: NodeValue,
    columnIndex: NumberValue,
    condition: StringValue,
    style: StringValue,
): NodeValue {
    val tableNode =
        table.unwrappedValue as? Table
            ?: throw IllegalArgumentException("Invalid argument: a table is required")

    val colIndex = columnIndex.unwrappedValue.toInt() - 1

    if (colIndex < 0 || colIndex >= tableNode.columns.size) {
        throw IllegalArgumentException("Invalid column index: must be between 1 and ${tableNode.columns.size}")
    }

    // Get the reference column for condition evaluation
    val conditionColumn = tableNode.columns[colIndex]

    // Extract text values from cells
    val conditionValues =
        conditionColumn.cells.map { cell ->
            cell.text.joinToString("") { node ->
                if (node is Text) node.text else ""
            }
        }

    // Parse the condition expression into operation and value
    val (operation, value) = parseFilterExpression(condition.unwrappedValue)

    // Find indices of rows that match the condition
    val matchingRows =
        conditionValues
            .withIndex()
            .filter { (_, cellValue) -> applyFilter(cellValue, operation, value) }
            .map { it.index }
            .toSet()

    // Parse the style expression into type and value
    val (styleType, styleValue) = parseStyleExpression(style.unwrappedValue)

    // Create new columns with styles applied to matching rows
    val newColumns =
        tableNode.columns.map { column ->
            val newCells =
                column.cells.mapIndexed { rowIndex, cell ->
                    if (rowIndex in matchingRows) {
                        applyStyleToCell(cell, styleType, styleValue)
                    } else {
                        cell
                    }
                }

            Table.Column(
                alignment = column.alignment,
                header = column.header,
                cells = newCells,
            )
        }

    return Table(newColumns, tableNode.caption).wrappedAsValue()
}

/**
 * Parses a filter expression and decomposes it into operation and value.
 *
 * This is a utility function that extracts the operation type and the operand
 * from filter expressions such as ">100", "contains:text", etc.
 *
 * Supported formats:
 * - contains:text - Text contains "text"
 * - date:>YYYY-MM-DD - Date greater than the specified date
 * - date:<YYYY-MM-DD - Date less than the specified date
 * - >N - Number greater than N
 * - <N - Number less than N
 * - =value - Exact match
 * - regex:pattern - Regular expression pattern match
 * - any other value - Case-insensitive equality
 */
private fun parseFilterExpression(expression: String): Pair<String, String> =
    when {
        expression.startsWith("contains:") -> "contains" to expression.substringAfter("contains:")
        expression.startsWith("date:>") -> "date:>" to expression.substringAfter("date:>")
        expression.startsWith("date:<") -> "date:<" to expression.substringAfter("date:<")
        expression.startsWith(">") -> ">" to expression.substringAfter(">")
        expression.startsWith("<") -> "<" to expression.substringAfter("<")
        expression.startsWith("=") -> "=" to expression.substringAfter("=")
        expression.startsWith("regex:") -> "regex" to expression.substringAfter("regex:")
        else -> "equals" to expression
    }

/**
 * Parses a style expression and decomposes it into type and value.
 *
 * This is a utility function that extracts the style type and the style value
 * from style expressions such as "background:red", "bold", etc.
 *
 * Supported formats:
 * - background:color - Sets background color
 * - color:color - Sets text color
 * - bold - Makes text bold
 * - italic - Makes text italic
 * - underline - Adds underline to text
 * - strike - Adds strikethrough to text
 * - align:value - Sets text alignment
 * - any other value - Used as a custom CSS style
 */
private fun parseStyleExpression(expression: String): Pair<String, String> =
    when {
        expression.startsWith("background:") -> "background" to expression.substringAfter("background:")
        expression.startsWith("color:") -> "color" to expression.substringAfter("color:")
        expression.startsWith("bold") -> "bold" to ""
        expression.startsWith("italic") -> "italic" to ""
        expression.startsWith("underline") -> "underline" to ""
        expression.startsWith("strike") -> "strike" to ""
        expression.startsWith("align:") -> "align" to expression.substringAfter("align:")
        else -> "custom" to expression
    }

/**
 * Applies a specific style to a cell of the table.
 *
 * This function generates appropriate HTML elements to represent the requested style,
 * wrapping the cell content with the appropriate markup. The HTML is inserted as text
 * content which will be recognized and rendered properly by the HTML renderer.
 *
 * For instance, to make text bold, it wraps the content in <strong> tags.
 * For custom styles, it uses span elements with appropriate attributes.
 *
 * @param cell The cell to which the style should be applied
 * @param styleType Type of style (background, color, bold, etc.)
 * @param styleValue Value for the style (e.g., color code, alignment value)
 * @return A new cell with the style applied
 */
private fun applyStyleToCell(
    cell: Table.Cell,
    styleType: String,
    styleValue: String,
): Table.Cell {
    val cellContent =
        cell.text.joinToString("") {
            if (it is Text) it.text else ""
        }

    // Create styled text based on the style type
    val styledText =
        when (styleType) {
            "background" -> {
                val html = "<span data-qd-style=\"background-color:$styleValue\">$cellContent</span>"
                listOf(Text(html))
            }

            "color" -> {
                val html = "<span data-qd-style=\"color:$styleValue\">$cellContent</span>"
                listOf(Text(html))
            }

            "bold" -> {
                val html = "<strong>$cellContent</strong>"
                listOf(Text(html))
            }

            "italic" -> {
                val html = "<em>$cellContent</em>"
                listOf(Text(html))
            }

            "underline" -> {
                val html = "<u>$cellContent</u>"
                listOf(Text(html))
            }

            "strike" -> {
                val html = "<s>$cellContent</s>"
                listOf(Text(html))
            }

            "align" -> {
                val html = "<span data-qd-style=\"text-align:$styleValue\">$cellContent</span>"
                listOf(Text(html))
            }

            "custom" -> {
                val html = "<span style=\"$styleValue\">$cellContent</span>"
                listOf(Text(html))
            }

            else -> cell.text // Unknown style
        }

    return Table.Cell(styledText)
}

/**
 * Applies a filter to a cell value using the specified operation.
 *
 * This function evaluates if a cell value matches the given filter criteria.
 * It supports various comparison operations for text, numbers, dates, and regular expressions.
 *
 * Supported operations:
 * - contains: Case-insensitive substring match
 * - date:> and date:<: Simple date comparison
 * - > and <: Numeric comparison
 * - =: Exact equality
 * - regex: Regular expression matching
 * - equals: Case-insensitive equality
 *
 * @param cellValue The string value from the cell
 * @param operation The comparison operation
 * @param filterValue The value to compare against
 * @return true if the cell matches the filter, false otherwise
 */
private fun applyFilter(
    cellValue: String,
    operation: String,
    filterValue: String,
): Boolean =
    when (operation) {
        "contains" -> cellValue.contains(filterValue, ignoreCase = true)
        "date:>" -> {
            try {
                cellValue > filterValue // This could be improved
            } catch (e: Exception) {
                false
            }
        }

        "date:<" -> {
            try {
                cellValue < filterValue // This one too
            } catch (e: Exception) {
                false
            }
        }

        ">" -> {
            val cellNumber = cellValue.toDoubleOrNull()
            val filterNumber = filterValue.toDoubleOrNull()
            if (cellNumber != null && filterNumber != null) {
                cellNumber > filterNumber
            } else {
                false
            }
        }

        "<" -> {
            val cellNumber = cellValue.toDoubleOrNull()
            val filterNumber = filterValue.toDoubleOrNull()
            if (cellNumber != null && filterNumber != null) {
                cellNumber < filterNumber
            } else {
                false
            }
        }

        "=" -> cellValue == filterValue
        "regex" -> cellValue.matches(Regex(filterValue))
        "equals" -> cellValue.equals(filterValue, ignoreCase = true)
        else -> false // Unknown operation
    }
