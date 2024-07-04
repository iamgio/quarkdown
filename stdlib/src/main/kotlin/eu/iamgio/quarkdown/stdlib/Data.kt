package eu.iamgio.quarkdown.stdlib

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.error.FunctionRuntimeException
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.function.value.data.subList
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import java.io.File
import kotlin.io.path.Path

/**
 * `Data` stdlib module exporter.
 * This module handles content fetched from external resources.
 */
val Data: Module =
    setOf(
        ::fileContent,
        ::csv,
    )

/**
 * @param path path of the file, relative or absolute (with extension)
 * @param requireExistance whether the corresponding file must exist
 * @return a [File] instance of the file located in [path].
 *         If the path is relative, the location is determined by the working directory of the pipeline.
 * @throws FunctionRuntimeException if the file does not exist and [requireExistance] is `true`
 */
internal fun file(
    context: Context,
    path: String,
    requireExistance: Boolean = true,
): File {
    val workingDirectory = context.attachedPipeline?.options?.workingDirectory

    val file =
        if (workingDirectory != null && !Path(path).isAbsolute) {
            File(workingDirectory, path)
        } else {
            File(path)
        }

    if (requireExistance && !file.exists()) {
        throw FunctionRuntimeException("File $file does not exist.")
    }

    return file
}

/**
 * @param path path of the file (with extension)
 * @param lineRange range of lines to extract from the file.
 *                  If not specified or infinite, the whole file is read
 * @return a string value of the text extracted from the file
 */
@Name("filecontent")
fun fileContent(
    @Injected context: Context,
    path: String,
    @Name("lines") lineRange: Range = Range.INFINITE,
): StringValue {
    val file = file(context, path)

    // If the range is infinite on both ends, the whole file is read.
    if (lineRange.isInfinite) {
        return StringValue(file.readText())
    }

    // Lines from the file in the given range.
    val lines = file.readLines().subList(lineRange)

    return lines.joinToString(System.lineSeparator()).wrappedAsValue()
}

/**
 * @param path path of the CSV file (with extension) to show
 * @return a table whose content is loaded from the file located in [path]
 */
fun csv(
    @Injected context: Context,
    path: String,
): NodeValue {
    val file = file(context, path)
    val columns = mutableMapOf<String, MutableList<String>>()

    // CSV is read row-by-row, while the Table is built by columns.
    csvReader().open(file) {
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

    return table.wrappedAsValue()
}
