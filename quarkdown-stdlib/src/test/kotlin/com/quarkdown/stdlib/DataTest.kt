package com.quarkdown.stdlib

import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.util.toPlainText
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

private const val DATA_FOLDER = "src/test/resources/data"

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
            "Line 1\nLine 2\n\nLine 4\nLine 5",
            read(context, path).unwrappedValue,
        )

        assertEquals(
            "Line 2\n\nLine 4",
            read(context, path, Range(2, 4)).unwrappedValue,
        )

        assertEquals(
            "Line 1\nLine 2",
            read(context, path, Range(null, 2)).unwrappedValue,
        )

        assertEquals(
            "Line 4\nLine 5",
            read(context, path, Range(4, null)).unwrappedValue,
        )

        // Out of bounds ranges.
        assertFails { read(context, path, Range(1, 8)) }
        assertFails { read(context, path, Range(0, 3)) }
        assertFails { read(context, path, Range(null, 9)) }
        assertFails { read(context, path, Range(9, null)) }
    }

    @Test
    fun `csv table`() {
        val path = "people.csv"
        val table = csv(context, path)

        assertIs<Table>(table.unwrappedValue)

        val columns = (table.unwrappedValue as Table).columns.iterator()

        with(columns.next()) {
            assertEquals("Name", (header.text.first() as Text).text)
            with(cells.iterator()) {
                assertEquals("Alex", next().text.toPlainText())
                assertEquals("Bert", next().text.toPlainText())
                assertEquals("Carl", next().text.toPlainText())
                assertEquals("Dave", next().text.toPlainText())
                assertEquals("Elly", next().text.toPlainText())
                assertEquals("Fran", next().text.toPlainText())
                assertEquals("Gwen", next().text.toPlainText())
                assertEquals("Hank", next().text.toPlainText())
                assertEquals("Ivan", next().text.toPlainText())
            }
        }

        with(columns.next()) {
            assertEquals("Sex", header.text.toPlainText())
            with(cells.iterator()) {
                repeat(4) {
                    assertEquals("M", next().text.toPlainText())
                }
                repeat(3) {
                    assertEquals("F", next().text.toPlainText())
                }
                repeat(2) {
                    assertEquals("M", next().text.toPlainText())
                }
            }
        }

        with(columns.next()) {
            assertEquals("Age", header.text.toPlainText())
            with(cells.iterator()) {
                assertEquals("41", next().text.toPlainText())
                assertEquals("42", next().text.toPlainText())
                assertEquals("32", next().text.toPlainText())
                assertEquals("39", next().text.toPlainText())
                assertEquals("30", next().text.toPlainText())
                assertEquals("33", next().text.toPlainText())
                assertEquals("26", next().text.toPlainText())
                assertEquals("30", next().text.toPlainText())
                assertEquals("53", next().text.toPlainText())
            }
        }

        with(columns.next()) {
            assertEquals("Height (in)", header.text.toPlainText())
            with(cells.iterator()) {
                assertEquals("74", next().text.toPlainText())
                assertEquals("68", next().text.toPlainText())
                assertEquals("70", next().text.toPlainText())
                assertEquals("72", next().text.toPlainText())
                assertEquals("66", next().text.toPlainText())
                assertEquals("66", next().text.toPlainText())
                assertEquals("64", next().text.toPlainText())
                assertEquals("71", next().text.toPlainText())
                assertEquals("72", next().text.toPlainText())
            }
        }

        with(columns.next()) {
            assertEquals("Weight (lbs)", header.text.toPlainText())
            with(cells.iterator()) {
                assertEquals("170", next().text.toPlainText())
                assertEquals("166", next().text.toPlainText())
                assertEquals("155", next().text.toPlainText())
                assertEquals("167", next().text.toPlainText())
                assertEquals("124", next().text.toPlainText())
                assertEquals("115", next().text.toPlainText())
                assertEquals("121", next().text.toPlainText())
                assertEquals("158", next().text.toPlainText())
                assertEquals("175", next().text.toPlainText())
            }
        }
    }
}
