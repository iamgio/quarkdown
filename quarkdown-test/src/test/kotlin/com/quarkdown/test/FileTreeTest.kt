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
}
