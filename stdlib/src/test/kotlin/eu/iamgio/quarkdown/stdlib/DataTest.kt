package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Table
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.value.data.Range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val DATA_FOLDER = "src/test/resources/data"

private val LINE_SEPARATOR = System.lineSeparator()

/**
 * [Data] module tests.
 */
class DataTest {
    @Test
    fun `file contents`() {
        val path = "$DATA_FOLDER/test.txt"

        assertEquals(
            "Line 1${LINE_SEPARATOR}Line 2${LINE_SEPARATOR}${LINE_SEPARATOR}Line 4${LINE_SEPARATOR}Line 5",
            fileContent(path).unwrappedValue,
        )

        assertEquals(
            "Line 2${LINE_SEPARATOR}${LINE_SEPARATOR}Line 4",
            fileContent(path, Range(1, 3)).unwrappedValue,
        )

        assertEquals(
            "Line 1${LINE_SEPARATOR}Line 2",
            fileContent(path, Range(null, 1)).unwrappedValue,
        )

        assertEquals(
            "Line 4${LINE_SEPARATOR}Line 5",
            fileContent(path, Range(3, null)).unwrappedValue,
        )
    }

    @Test
    fun `csv table`() {
        val path = "$DATA_FOLDER/people.csv"
        val table = csv(path)

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
