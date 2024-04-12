package eu.iamgio.quarkdown.stdlib

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.value.NodeValue

/**
 * `Data` stdlib module exporter.
 */
val Data =
    setOf(
        ::csv,
    )

/**
 * @param path path of the CSV file (with extension) to show
 * @return a table whose content is loaded from the file located in [path]
 */
fun csv(path: String): NodeValue {
    val columns = mutableMapOf<String, MutableList<String>>()

    // CSV is read row-by-row, while the Table is built by columns.
    csvReader().open(path) {
        readAllWithHeaderAsSequence()
            .flatMap { it.entries }
            .forEach { (header, content) ->
                val cells = columns.computeIfAbsent(header) { mutableListOf() }
                cells += content
            }
    }

    val table =
        Table(
            columns.map { (header, cells) ->
                Table.Column(
                    Table.Alignment.NONE,
                    Table.Cell(listOf(Text(header.trim()))),
                    cells.map { cell -> Table.Cell(listOf(Text(cell.trim()))) },
                )
            },
        )

    return NodeValue(table)
}
