package com.quarkdown.stdlib

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.dsl.buildBlocks
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.ast.quarkdown.block.FileTreeEntry
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [fileTree] generation from Markdown lists.
 */
class FileTreeTest {
    private fun buildFileTree(blocks: List<Node>) = fileTree(MarkdownContent(blocks)).unwrappedValue as FileTree

    @Test
    fun `only top-level files`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { text("file1.txt") } }
                    listItem { paragraph { text("file2.json") } }
                    listItem { paragraph { text("file3.csv") } }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.File("file1.txt"),
                FileTreeEntry.File("file2.json"),
                FileTreeEntry.File("file3.csv"),
            ),
            tree.entries,
        )
    }

    @Test
    fun `single directory`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("dir1") }
                        unorderedList(loose = false) {
                            listItem { paragraph { text("file1.txt") } }
                            listItem { paragraph { text("file2.json") } }
                        }
                    }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.Directory(
                    "dir1",
                    listOf(
                        FileTreeEntry.File("file1.txt"),
                        FileTreeEntry.File("file2.json"),
                    ),
                ),
            ),
            tree.entries,
        )
    }

    @Test
    fun `nested directories`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("src") }
                        unorderedList(loose = false) {
                            listItem {
                                paragraph { text("components") }
                                unorderedList(loose = false) {
                                    listItem { paragraph { text("Button.ts") } }
                                    listItem { paragraph { text("Card.ts") } }
                                }
                            }
                            listItem { paragraph { text("index.ts") } }
                        }
                    }
                    listItem { paragraph { text("README.md") } }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.Directory(
                    "src",
                    listOf(
                        FileTreeEntry.Directory(
                            "components",
                            listOf(
                                FileTreeEntry.File("Button.ts"),
                                FileTreeEntry.File("Card.ts"),
                            ),
                        ),
                        FileTreeEntry.File("index.ts"),
                    ),
                ),
                FileTreeEntry.File("README.md"),
            ),
            tree.entries,
        )
    }

    @Test
    fun `top-level ellipsis`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { text("file1.txt") } }
                    listItem { paragraph { text("...") } }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.File("file1.txt"),
                FileTreeEntry.Ellipsis(),
            ),
            tree.entries,
        )
    }

    @Test
    fun `ellipsis inside directory`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("src") }
                        unorderedList(loose = false) {
                            listItem { paragraph { text("main.ts") } }
                            listItem { paragraph { text("...") } }
                        }
                    }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.Directory(
                    "src",
                    listOf(
                        FileTreeEntry.File("main.ts"),
                        FileTreeEntry.Ellipsis(),
                    ),
                ),
            ),
            tree.entries,
        )
    }

    @Test
    fun `highlighted file`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { text("file1.txt") } }
                    listItem { paragraph { strong { text("file2.txt") } } }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.File("file1.txt"),
                FileTreeEntry.File("file2.txt", highlighted = true),
            ),
            tree.entries,
        )
    }

    @Test
    fun `highlighted directory`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { strong { text("src") } }
                        unorderedList(loose = false) {
                            listItem { paragraph { text("file1.txt") } }
                            listItem { paragraph { text("file2.txt") } }
                        }
                    }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.Directory(
                    "src",
                    listOf(
                        FileTreeEntry.File("file1.txt"),
                        FileTreeEntry.File("file2.txt"),
                    ),
                    highlighted = true,
                ),
            ),
            tree.entries,
        )
    }

    @Test
    fun `highlighted ellipsis`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { text("file1.txt") } }
                    listItem { paragraph { strong { text("...") } } }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.File("file1.txt"),
                FileTreeEntry.Ellipsis(highlighted = true),
            ),
            tree.entries,
        )
    }

    @Test
    fun `multiple highlighted entries`() {
        val tree =
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { strong { text("file1.txt") } } }
                    listItem {
                        paragraph { strong { text("src") } }
                        unorderedList(loose = false) {
                            listItem { paragraph { strong { text("main.ts") } } }
                            listItem { paragraph { text("utils.ts") } }
                        }
                    }
                }
            }.let(::buildFileTree)

        assertEquals(
            listOf(
                FileTreeEntry.File("file1.txt", highlighted = true),
                FileTreeEntry.Directory(
                    "src",
                    listOf(
                        FileTreeEntry.File("main.ts", highlighted = true),
                        FileTreeEntry.File("utils.ts"),
                    ),
                    highlighted = true,
                ),
            ),
            tree.entries,
        )
    }
}
