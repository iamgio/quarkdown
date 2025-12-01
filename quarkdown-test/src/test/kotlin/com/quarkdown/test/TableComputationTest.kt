package com.quarkdown.test

import com.quarkdown.core.util.indent
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for table computation and operations.
 */
class TableComputationTest {
    private val table =
        """
        | Name    | Age | City |
        |---------|-----|------|
        | John    | 25  | NY   |
        | Barbara | 102  | SF   |
        | Lisa    | 32  | LA   |
        | Mike    | 19  | CHI  |
        """.trimIndent().indent("\t")

    private val john = "<tr><td>John</td><td>25</td><td>NY</td></tr>"
    private val barbara = "<tr><td>Barbara</td><td>102</td><td>SF</td></tr>"
    private val lisa = "<tr><td>Lisa</td><td>32</td><td>LA</td></tr>"
    private val mike = "<tr><td>Mike</td><td>19</td><td>CHI</td></tr>"

    private fun htmlTable(
        htmlContent: String,
        caption: String? = null,
    ) = "<table><thead><tr><th>Name</th><th>Age</th><th>City</th></tr></thead>" +
        "<tbody>$htmlContent</tbody>" +
        (caption?.let { "<caption class=\"caption-bottom\">$it</caption>" } ?: "") +
        "</table>"

    @Test
    fun `plain sorting, ascending`() {
        execute(".tablesort column:{2}\n$table") {
            assertEquals(
                htmlTable(mike + john + lisa + barbara),
                it,
            )
        }
    }

    @Test
    fun `plain sorting, descending`() {
        execute(".tablesort column:{2} order:{descending}\n$table") {
            assertEquals(
                htmlTable(barbara + lisa + john + mike),
                it,
            )
        }
    }

    @Test
    fun `plain filtering`() {
        execute(".tablefilter {2} {@lambda x: .x::isgreater {20}}\n$table") {
            assertEquals(
                htmlTable(john + barbara + lisa),
                it,
            )
        }
    }

    @Test
    fun `plain sum computing`() {
        execute(".tablecompute {2} {@lambda x: .x::sumall}\n$table") {
            assertEquals(
                htmlTable(
                    john + barbara + lisa + mike +
                        "<tr><td></td><td>178</td><td></td></tr>",
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
                    john + barbara + lisa + mike +
                        "<tr><td></td><td>44</td><td></td></tr>",
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
                    mike + john + lisa + barbara +
                        "<tr><td></td><td>44</td><td></td></tr>",
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
    fun `csv with caption`() {
        execute(".csv {csv/people.csv} caption:{People}") {
            assertEquals(
                htmlTable(
                    john + lisa + mike,
                    caption = "People",
                ),
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

    @Test
    fun `get column`() {
        execute(
            ".var {col}\n" +
                "\t.tablecolumn {2}\n" +
                table.indent("\t\t") +
                """
                .foreach {.col}
                    Cell = .1
                """.trimIndent(),
        ) {
            assertEquals(
                "<p>Cell = 25</p>" +
                    "<p>Cell = 102</p>" +
                    "<p>Cell = 32</p>" +
                    "<p>Cell = 19</p>",
                it,
            )
        }
    }

    @Test
    fun `get columns`() {
        execute(
            ".var {cols}\n" +
                "\t.tablecolumns\n" +
                table.indent("\t\t") +
                "\n.cols::size",
        ) {
            assertEquals(
                "<p>3</p>",
                it,
            )
        }
    }

    @Test
    fun `sum column of csv`() {
        execute(
            """
            .var {col}
                .tablecolumn {2}
                    .csv {csv/people.csv}
            
            **.col::sumall**
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>76</strong></p>", it)
        }
    }

    @Test
    fun `generation by rows, no headers`() {
        execute(
            """
            .tablebyrows
                - - John
                  - 25
                  - NY
                - - Lisa
                  - 32
                  - LA
                - - Mike
                  - 19
                  - CHI
            """.trimIndent(),
        ) {
            assertEquals(
                htmlTable(john + lisa + mike)
                    .replace(
                        "<thead><tr><th>Name</th><th>Age</th><th>City</th></tr></thead>",
                        "<thead><tr><th></th><th></th><th></th></tr></thead>",
                    ),
                it,
            )
        }
    }

    @Test
    fun `generation by rows, with headers`() {
        execute(
            """
            .var {headers}
                - Name
                - Age
                - City
                  
            .tablebyrows {.headers}
                - - John
                  - 25
                  - NY
                - - Lisa
                  - 32
                  - LA
                - - Mike
                  - 19
                  - CHI
            """.trimIndent(),
        ) {
            assertEquals(
                htmlTable(john + lisa + mike),
                it,
            )
        }
    }
}
