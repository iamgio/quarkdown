package com.quarkdown.stdlib

import com.quarkdown.core.assertNodeEquals
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.util.toPlainText
import com.quarkdown.stdlib.internal.Ordering
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val DATA_FOLDER = "src/test/resources/data"
private const val LIST_FILES_FOLDER = "listfiles"

/**
 * [Data] module tests.
 */
class DataTest {
    private val context = MutableContext(QuarkdownFlavor)

    @BeforeTest
    fun setup() {
        // Attach a mock pipeline to the context, in order to set a working directory for the function calls to use.
        val options = PipelineOptions(workingDirectory = File(DATA_FOLDER))
        context.attachMockPipeline(options)
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

    @Test
    fun `csv table, as plain text cells`() {
        val path = "drinks.csv"
        val table = csv(context, path, mode = CsvParsingMode.PLAIN).unwrappedValue
        assertIs<Table>(table)

        val columns = table.columns.iterator()
        with(columns.next()) {
            assertEquals("Name", (header.text.first() as Text).text)
            with(cells.iterator()) {
                assertEquals("Alice", (next().text.first() as Text).text)
                assertEquals("Bob", (next().text.first() as Text).text)
            }
        }
        with(columns.next()) {
            assertEquals("*Favorite* drink", (header.text.first() as Text).text)
            with(cells.iterator()) {
                assertEquals("**Coffee**", (next().text.first() as Text).text)
                assertEquals("***Pepsi***", (next().text.first() as Text).text)
            }
        }
    }

    @Test
    fun `csv table, as markdown cells`() {
        val path = "drinks.csv"
        val table = csv(context, path, mode = CsvParsingMode.MARKDOWN).unwrappedValue
        assertIs<Table>(table)

        val columns = table.columns.iterator()
        with(columns.next()) {
            assertEquals("Name", (header.text.first() as Text).text)
            with(cells.iterator()) {
                assertEquals("Alice", (next().text.first() as Text).text)
                assertEquals("Bob", (next().text.first() as Text).text)
            }
        }
        with(columns.next()) {
            assertNodeEquals(
                buildInline {
                    emphasis { text("Favorite") }
                    text(" drink")
                },
                header.text,
            )
            with(cells.iterator()) {
                assertNodeEquals(buildInline { strong { text("Coffee") } }, next().text)
                assertNodeEquals(buildInline { strongEmphasis { text("Pepsi") } }, next().text)
            }
        }
    }

    @Test
    fun `list files unsorted`() {
        val files = listFiles(context, LIST_FILES_FOLDER, fullPath = false)
        val names = files.unwrappedValue.map { it.unwrappedValue }.toSet()
        assertEquals(setOf("a.txt", "b.txt", "c.txt", "d"), names)
    }

    @Test
    fun `list files sorted by name ascending`() {
        val files =
            listFiles(
                context,
                LIST_FILES_FOLDER,
                fullPath = false,
                sortBy = FileSorting.NAME,
            )
        val names = files.unwrappedValue.map { it.unwrappedValue }.toList()
        assertEquals(listOf("a.txt", "b.txt", "c.txt", "d"), names)
    }

    @Test
    fun `list files sorted by name descending`() {
        val files =
            listFiles(
                context,
                LIST_FILES_FOLDER,
                fullPath = false,
                sortBy = FileSorting.NAME,
                order = Ordering.DESCENDING,
            )
        val names = files.unwrappedValue.map { it.unwrappedValue }.toList()
        assertEquals(listOf("d", "c.txt", "b.txt", "a.txt"), names)
    }

    @Test
    fun `list non-directory files`() {
        val files =
            listFiles(
                context,
                LIST_FILES_FOLDER,
                listDirectories = false,
                fullPath = false,
                sortBy = FileSorting.NAME,
            )
        val names = files.unwrappedValue.map { it.unwrappedValue }.toList()
        assertEquals(listOf("a.txt", "b.txt", "c.txt"), names)
    }

    @Test
    fun `list files with full path`() {
        val files = listFiles(context, LIST_FILES_FOLDER, fullPath = true, sortBy = FileSorting.NAME)
        val paths = files.unwrappedValue.map { it.unwrappedValue }.toList()
        paths.forEach { path ->
            assertContains(path, LIST_FILES_FOLDER)
            assertTrue(
                path.endsWith("a.txt") ||
                    path.endsWith("b.txt") ||
                    path.endsWith("c.txt") ||
                    path.endsWith("d"),
            )
            assertTrue(File(path).isAbsolute)
        }
    }

    @Test
    fun `list files non-existent directory`() {
        assertFails { listFiles(context, "nonexistent") }
    }

    @Test
    fun `list files on a file instead of directory`() {
        assertFails { listFiles(context, "test.txt") }
    }

    @Test
    fun `get file name with extension`() {
        val name = fileName(context, "listfiles/a.txt", includeExtension = true)
        assertEquals("a.txt", name.unwrappedValue)
    }

    @Test
    fun `get file name without extension`() {
        val name = fileName(context, "listfiles/a.txt", includeExtension = false)
        assertEquals("a", name.unwrappedValue)
    }

    @Test
    fun `get file name non-existent file`() {
        assertFails { fileName(context, "listfiles/nonexistent.txt") }
    }
}
