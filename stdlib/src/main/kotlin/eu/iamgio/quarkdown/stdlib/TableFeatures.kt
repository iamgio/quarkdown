package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
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
 * @param table table to sort
 * @return the sorted table
 */
@Name("tablesort")
fun tableSort(
    column: Int,
    order: TableSortOrder = TableSortOrder.ASCENDING,
    table: MarkdownContent,
): NodeValue {
    val tableNode =
        table.children.firstOrNull() as? Table
            ?: throw IllegalArgumentException("Invalid argument: a table is required")

    val columnIndex = column - 1

    require(columnIndex in 0 until tableNode.columns.size) {
        "Column index must be between 1 and ${tableNode.columns.size}"
    }

    val referenceColumn: Table.Column = tableNode.columns[columnIndex]

    // Extract text values from cells.
    val referenceValues =
        referenceColumn.cells.map { it.text.toPlainText() }

    // Obtain the indexes of the rows sorted by the reference column.
    val orderedRowIndexes: List<Int> =
        referenceValues
            .asSequence()
            .withIndex()
            .let { order.apply(it) { item -> item.value } }
            .map { it.index }
            .toList()

    // Create new columns with sorted rows.
    val newColumns =
        tableNode.columns.map {
            it.copy(cells = orderedRowIndexes.map(it.cells::get))
        }

    return Table(newColumns, tableNode.caption).wrappedAsValue()
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
 * @param table table to filter
 * @param columnIndex index of the column (1-based)
 * @param filterExpression filter expression
 * @return the filtered table
 */
@Name("tablefilter")
fun tableFilter(
    table: NodeValue,
    columnIndex: NumberValue,
    filterExpression: StringValue,
): NodeValue {
    val tableNode =
        table.unwrappedValue as? Table
            ?: throw IllegalArgumentException("Invalid argument: a table is required")

    val colIndex = columnIndex.unwrappedValue.toInt() - 1

    if (colIndex < 0 || colIndex >= tableNode.columns.size) {
        throw IllegalArgumentException("Invalid column index: must be between 1 and ${tableNode.columns.size}")
    }

    // Get the reference column for filtering
    val filterColumn = tableNode.columns[colIndex]

    // Extract text values from cells
    val filterValues =
        filterColumn.cells.map { cell ->
            cell.text.joinToString("") { node ->
                if (node is Text) node.text else ""
            }
        }

    // Parse the filter expression into operation and value
    val (operation, value) = parseFilterExpression(filterExpression.unwrappedValue)

    // Find indices of rows that match the filter
    val filteredIndices =
        filterValues
            .withIndex()
            .filter { (_, cellValue) -> applyFilter(cellValue, operation, value) }
            .map { it.index }

    // Create new columns with only the filtered rows
    val newColumns =
        tableNode.columns.map { column ->
            Table.Column(
                alignment = column.alignment,
                header = column.header,
                cells = filteredIndices.map { column.cells[it] },
            )
        }

    return Table(newColumns, tableNode.caption).wrappedAsValue()
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
    table: NodeValue,
    formula: StringValue,
    columnIndex: NumberValue,
): NodeValue {
    val tableNode =
        table.unwrappedValue as? Table
            ?: throw IllegalArgumentException("Invalid argument: a table is required")

    val colIndex = columnIndex.unwrappedValue.toInt() - 1

    if (colIndex < 0 || colIndex >= tableNode.columns.size) {
        throw IllegalArgumentException("Invalid column index: must be between 1 and ${tableNode.columns.size}")
    }

    // Get the reference column for computation
    val computeColumn = tableNode.columns[colIndex]

    // Extract numeric values from cells (non-numeric values become 0)
    val values =
        computeColumn.cells.map { cell ->
            val textValue =
                cell.text.joinToString("") { node ->
                    if (node is Text) node.text else ""
                }
            textValue.toDoubleOrNull() ?: 0.0
        }

    // Apply the selected formula to the values
    val result =
        when (formula.unwrappedValue.uppercase()) {
            "SUM" -> values.sum()
            "AVG" -> if (values.isNotEmpty()) values.average() else 0.0
            "COUNT" -> values.size.toDouble()
            "MIN" -> if (values.isNotEmpty()) values.minOrNull() ?: 0.0 else 0.0
            "MAX" -> if (values.isNotEmpty()) values.maxOrNull() ?: 0.0 else 0.0
            else -> 0.0
        }

    val resultText =
        if (result == result.toInt().toDouble()) {
            result.toInt().toString()
        } else {
            "%.2f".format(result)
        }

    // Create new columns with the computation result added
    val newColumns =
        tableNode.columns.mapIndexed { index, column ->
            val resultCell =
                if (index == colIndex) {
                    Table.Cell(listOf(Text(resultText)))
                } else {
                    Table.Cell(listOf(Text("")))
                }

            // Add formula label in the first column
            val firstColumnCell =
                if (index == 0) {
                    Table.Cell(listOf(Text(formula.unwrappedValue.uppercase())))
                } else {
                    resultCell
                }

            Table.Column(
                alignment = column.alignment,
                header = column.header,
                cells = column.cells + if (index == 0) firstColumnCell else resultCell,
            )
        }

    return Table(newColumns, tableNode.caption).wrappedAsValue()
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
