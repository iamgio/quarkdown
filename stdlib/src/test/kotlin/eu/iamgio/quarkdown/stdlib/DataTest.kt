package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.value.data.Range
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val DATA_FOLDER = "src/test/resources/data"

private val LINE_SEPARATOR = System.lineSeparator()

/**
 * [Data] module tests.
 */
class DataTest {
    private val context = MutableContext(QuarkdownFlavor)

    @BeforeTest
    fun setup() {
        // Attach a mock pipeline to the context, in order to set a working directory for the function calls to use.
        val options = PipelineOptions(workingDirectory = File(DATA_FOLDER))
        Pipeline(context, options, emptySet(), { _, _ -> throw UnsupportedOperationException() })
    }

    @Test
    fun `file contents`() {
        val path = "test.txt"

        assertEquals(
            "Line 1${LINE_SEPARATOR}Line 2${LINE_SEPARATOR}${LINE_SEPARATOR}Line 4${LINE_SEPARATOR}Line 5",
            read(context, path).unwrappedValue,
        )

        assertEquals(
            "Line 2${LINE_SEPARATOR}${LINE_SEPARATOR}Line 4",
            read(context, path, Range(2, 4)).unwrappedValue,
        )

        assertEquals(
            "Line 1${LINE_SEPARATOR}Line 2",
            read(context, path, Range(null, 2)).unwrappedValue,
        )

        assertEquals(
            "Line 4${LINE_SEPARATOR}Line 5",
            read(context, path, Range(4, null)).unwrappedValue,
        )
    }

    @Test
    fun `csv table`() {
        val path = "people.csv"
        val table = csv(context, path)

        assertIs<Table>(table.unwrappedValue)

        val columns = (table.unwrappedValue as Table).columns.iterator()

        with(columns.next()) {
            assertEquals(Text("Name"), header.text.first())
            with(cells.iterator()) {
                assertEquals(Text("Alex"), next().text.first())
                assertEquals(Text("Bert"), next().text.first())
                assertEquals(Text("Carl"), next().text.first())
                assertEquals(Text("Dave"), next().text.first())
                assertEquals(Text("Elly"), next().text.first())
                assertEquals(Text("Fran"), next().text.first())
                assertEquals(Text("Gwen"), next().text.first())
                assertEquals(Text("Hank"), next().text.first())
                assertEquals(Text("Ivan"), next().text.first())
            }
        }

        with(columns.next()) {
            assertEquals(Text("Sex"), header.text.first())
            with(cells.iterator()) {
                repeat(4) {
                    assertEquals(Text("M"), next().text.first())
                }
                repeat(3) {
                    assertEquals(Text("F"), next().text.first())
                }
                repeat(2) {
                    assertEquals(Text("M"), next().text.first())
                }
            }
        }

        with(columns.next()) {
            assertEquals(Text("Age"), header.text.first())
            with(cells.iterator()) {
                assertEquals(Text("41"), next().text.first())
                assertEquals(Text("42"), next().text.first())
                assertEquals(Text("32"), next().text.first())
                assertEquals(Text("39"), next().text.first())
                assertEquals(Text("30"), next().text.first())
                assertEquals(Text("33"), next().text.first())
                assertEquals(Text("26"), next().text.first())
                assertEquals(Text("30"), next().text.first())
                assertEquals(Text("53"), next().text.first())
            }
        }

        with(columns.next()) {
            assertEquals(Text("Height (in)"), header.text.first())
            with(cells.iterator()) {
                assertEquals(Text("74"), next().text.first())
                assertEquals(Text("68"), next().text.first())
                assertEquals(Text("70"), next().text.first())
                assertEquals(Text("72"), next().text.first())
                assertEquals(Text("66"), next().text.first())
                assertEquals(Text("66"), next().text.first())
                assertEquals(Text("64"), next().text.first())
                assertEquals(Text("71"), next().text.first())
                assertEquals(Text("72"), next().text.first())
            }
        }

        with(columns.next()) {
            assertEquals(Text("Weight (lbs)"), header.text.first())
            with(cells.iterator()) {
                assertEquals(Text("170"), next().text.first())
                assertEquals(Text("166"), next().text.first())
                assertEquals(Text("155"), next().text.first())
                assertEquals(Text("167"), next().text.first())
                assertEquals(Text("124"), next().text.first())
                assertEquals(Text("115"), next().text.first())
                assertEquals(Text("121"), next().text.first())
                assertEquals(Text("158"), next().text.first())
                assertEquals(Text("175"), next().text.first())
            }
        }
    }
}
