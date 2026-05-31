package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for file tree generation and rendering.
 */
class FileTreeTest {
    @Test
    fun `files only`() {
        execute(
            """
            .filetree
                - file1.txt
                - file2.json
                - file3.csv
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"file\" data-name=\"file1.txt\">file1.txt</li>" +
                    "<li class=\"file\" data-name=\"file2.json\">file2.json</li>" +
                    "<li class=\"file\" data-name=\"file3.csv\">file3.csv</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun `single directory`() {
        execute(
            """
            .filetree
                - src
                  - main.ts
                  - utils.ts
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"directory\" data-name=\"src\">src" +
                    "<ul>" +
                    "<li class=\"file\" data-name=\"main.ts\">main.ts</li>" +
                    "<li class=\"file\" data-name=\"utils.ts\">utils.ts</li>" +
                    "</ul>" +
                    "</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun `nested directories`() {
        execute(
            """
            .filetree
                - src
                  - components
                    - Button.ts
                  - index.ts
                - README.md
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"directory\" data-name=\"src\">src" +
                    "<ul>" +
                    "<li class=\"directory\" data-name=\"components\">components" +
                    "<ul>" +
                    "<li class=\"file\" data-name=\"Button.ts\">Button.ts</li>" +
                    "</ul>" +
                    "</li>" +
                    "<li class=\"file\" data-name=\"index.ts\">index.ts</li>" +
                    "</ul>" +
                    "</li>" +
                    "<li class=\"file\" data-name=\"README.md\">README.md</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun highlighted() {
        execute(
            """
            .filetree
                - file1.txt
                - **file2.txt**
                - **src**
                  - **main.ts**
                  - utils.ts
                - **...**
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"file\" data-name=\"file1.txt\">file1.txt</li>" +
                    "<li class=\"file\" data-name=\"file2.txt\" data-highlighted=\"\">file2.txt</li>" +
                    "<li class=\"directory\" data-name=\"src\" data-highlighted=\"\">src" +
                    "<ul>" +
                    "<li class=\"file\" data-name=\"main.ts\" data-highlighted=\"\">main.ts</li>" +
                    "<li class=\"file\" data-name=\"utils.ts\">utils.ts</li>" +
                    "</ul>" +
                    "</li>" +
                    "<li class=\"ellipsis\" data-highlighted=\"\">&hellip;</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun ellipsis() {
        execute(
            """
            .filetree
                - src
                  - main.ts
                  - ...
                - README.md
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"directory\" data-name=\"src\">src" +
                    "<ul>" +
                    "<li class=\"file\" data-name=\"main.ts\">main.ts</li>" +
                    "<li class=\"ellipsis\">&hellip;</li>" +
                    "</ul>" +
                    "</li>" +
                    "<li class=\"file\" data-name=\"README.md\">README.md</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun `explicit empty directory marker`() {
        execute(
            """
            .filetree
                - src/
                - target/
                - README.md
            """.trimIndent(),
        ) {
            assertHtmlEquals(
                """
                <div class="file-tree"><ul>
                    <li class="directory" data-name="src">src<ul></ul></li>
                    <li class="directory" data-name="target">target<ul></ul></li>
                    <li class="file" data-name="README.md">README.md</li>
                </ul></div>
                """.trimIndent(),
                it,
            )
        }
    }

    @Test
    fun `explicit directory marker with children`() {
        execute(
            """
            .filetree
                - docs/
                  - guide.md
                - docs
            """.trimIndent(),
        ) {
            assertHtmlEquals(
                """
                <div class="file-tree"><ul>
                    <li class="directory" data-name="docs">docs<ul>
                            <li class="file" data-name="guide.md">guide.md</li>
                        </ul></li>
                    <li class="file" data-name="docs">docs</li>
                </ul></div>
                """.trimIndent(),
                it,
            )
        }
    }

    @Test
    fun `single slash remains file`() {
        execute(
            """
            .filetree
                - /
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"file-tree\"><ul>" +
                    "<li class=\"file\" data-name=\"/\">/</li>" +
                    "</ul></div>",
                it,
            )
        }
    }

    @Test
    fun `explicit directory marker ignores trailing spaces`() {
        execute(
            """
            .filetree
                - src/   
                - docs
                  - guide.md
            """.trimIndent(),
        ) {
            assertHtmlEquals(
                """
                <div class="file-tree"><ul>
                    <li class="directory" data-name="src">src<ul></ul></li>
                    <li class="directory" data-name="docs">docs<ul>
                            <li class="file" data-name="guide.md">guide.md</li>
                        </ul></li>
                </ul></div>
                """.trimIndent(),
                it,
            )
        }
    }

    /**
     * Compares compacted HTML to keep test expectations readable while remaining strict on structure.
     */
    private fun assertHtmlEquals(
        expected: CharSequence,
        actual: CharSequence,
    ) = assertEquals(expected.compactHtml(), actual.compactHtml())

    /**
     * Removes layout whitespace between tags used only for test readability.
     */
    private fun CharSequence.compactHtml(): String = toString().trim().replace(Regex(">\\s+<"), "><")
}
