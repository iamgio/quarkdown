package eu.iamgio.quarkdown.stdlib

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue
import java.io.File

/**
 * `Data` stdlib module exporter.
 * This module handles content fetched from external resources.
 */
val Data =
    setOf(
        ::fileContent,
        ::csv,
    )

/**
 * @param path path of the file (with extension)
 * @return a string value of the text extracted from the file
 */
@FunctionName("filecontent")
fun fileContent(path: String): StringValue {
    return StringValue(File(path).readText())
}

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
