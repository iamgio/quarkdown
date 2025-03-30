package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.util.toPlainText

/**
 * `Table` stdlib module exporter.
 * This module provides advanced functionality for tables, enhancing their capabilities
 * beyond basic data representation. It adds dynamic operations like sorting, filtering,
 * calculations, and conditional styling.
 */
val TableComputation: Module =
    setOf(
        ::tableSort,
        ::tableFilter,
        ::tableCompute,
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
