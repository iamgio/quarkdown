package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import eu.iamgio.quarkdown.util.indent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for table computation and operations.
 */
class TableComputationTest {
    private val table =
        """
        | Name | Age | City |
        |------|-----|------|
        | John | 25  | NY   |
        | Lisa | 32  | LA   |
        | Mike | 19  | CHI  |
        """.trimIndent().indent("\t")

    private val john = "<tr><td>John</td><td>25</td><td>NY</td></tr>"
    private val lisa = "<tr><td>Lisa</td><td>32</td><td>LA</td></tr>"
    private val mike = "<tr><td>Mike</td><td>19</td><td>CHI</td></tr>"

    private fun htmlTable(htmlContent: String) =
        "<table><thead><tr><th>Name</th><th>Age</th><th>City</th></tr></thead>" +
            "<tbody>$htmlContent</tbody></table>"

    @Test
    fun `plain sorting, ascending`() {
        execute(".tablesort column:{2}\n$table") {
            assertEquals(
                htmlTable(mike + john + lisa),
                it,
            )
        }
    }

    @Test
    fun `plain sorting, descending`() {
        execute(".tablesort column:{2} order:{descending}\n$table") {
            assertEquals(
                htmlTable(lisa + john + mike),
                it,
            )
        }
    }

    @Test
    fun `plain filtering`() {
        execute(".tablefilter {2} {@lambda x: .x::isgreater {20}}\n$table") {
            assertEquals(
                htmlTable(john + lisa),
                it,
            )
        }
    }

    @Test
    fun `plain sum computing`() {
        execute(".tablecompute {2} {@lambda x: .x::sumall}\n$table") {
            assertEquals(
                htmlTable(
                    john + lisa + mike +
                        "<tr><td></td><td>76</td><td></td></tr>",
                ),
                it,
            )
        }
    }

    @Test
    fun `plain average computing`() {
        execute(".tablecompute {2} {@lambda .1::average::round}\n$table") {
            assertEquals(
                htmlTable(
                    john + lisa + mike +
                        "<tr><td></td><td>25</td><td></td></tr>",
                ),
                it,
            )
        }
    }

    @Test
    fun composition() {
        execute(
            ".tablecompute {2} {@lambda x: .x::average::round}\n" +
                "\t.tablesort {2}\n" +
                table.indent("\t"),
        ) {
            assertEquals(
                htmlTable(
                    mike + john + lisa +
                        "<tr><td></td><td>25</td><td></td></tr>",
                ),
                it,
            )
        }
    }

    @Test
    fun csv() {
        execute(".csv {csv/people.csv}") {
            assertEquals(
                htmlTable(john + lisa + mike),
                it,
            )
        }
    }

    @Test
    fun `compute on csv`() {
        execute(".tablecompute {2} {@lambda x: .x::average::round}\n\t.csv {csv/people.csv}") {
            assertEquals(
                htmlTable(
                    john + lisa + mike +
                        "<tr><td></td><td>25</td><td></td></tr>",
                ),
                it,
            )
        }
    }

    @Test
    fun `composition on csv`() {
        execute(
            ".tablecompute {2} {@lambda x: .x::average::round}\n" +
                "\t.tablesort {2}\n" +
                "\t\t.csv {csv/people.csv}",
        ) {
            assertEquals(
                htmlTable(
                    mike + john + lisa +
                        "<tr><td></td><td>25</td><td></td></tr>",
                ),
                it,
            )
        }
    }
}
