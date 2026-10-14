package com.quarkdown.pm.repo.github

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for [GitHubTree] decoding and [GitHubTreeIndex] queries.
 */
class GitHubTreeIndexTest {
    private fun blob(path: String) = GitHubTreeEntry(path = path, type = "blob", size = 1)

    private fun dir(path: String) = GitHubTreeEntry(path = path, type = "tree")

    private val tree =
        GitHubTree(
            sha = "abc",
            tree =
                listOf(
                    blob("README.md"),
                    dir("cv"),
                    dir("cv/1.0.0"),
                    blob("cv/1.0.0/atom.toml"),
                    blob("cv/1.0.0/main.qd"),
                    dir("cv/1.0.0/assets"),
                    blob("cv/1.0.0/assets/logo.png"),
                    dir("cv/1.1.0"),
                    blob("cv/1.1.0/atom.toml"),
                    blob("cv/notes.txt"),
                    dir("cvx"),
                    blob("cvx/1.0.0/atom.toml"),
                    GitHubTreeEntry(path = "cv/1.0.0/vendored", type = "commit"),
                ),
        )
    private val index = GitHubTreeIndex(tree)

    @Test
    fun `files under a directory are blobs only, recursively`() {
        assertEquals(
            listOf("cv/1.0.0/atom.toml", "cv/1.0.0/main.qd", "cv/1.0.0/assets/logo.png"),
            index.filesUnder("cv/1.0.0"),
        )
    }

    @Test
    fun `prefix match does not leak into sibling directories`() {
        assertTrue(index.filesUnder("cv").none { it.startsWith("cvx") })
    }

    @Test
    fun `unknown directory has no files`() {
        assertEquals(emptyList(), index.filesUnder("nope"))
    }

    @Test
    fun `the root lists top-level directories and every file`() {
        assertEquals(setOf("cv", "cvx"), index.childDirectoriesOf(""))
        assertEquals(7, index.filesUnder("").size)
    }

    @Test
    fun `child directories exclude direct files`() {
        assertEquals(setOf("1.0.0", "1.1.0"), index.childDirectoriesOf("cv"))
    }

    @Test
    fun `truncated tree is rejected`() {
        assertFailsWith<IllegalStateException> { GitHubTreeIndex(tree.copy(truncated = true)) }
    }

    @Test
    fun `decodes api response ignoring unknown keys`() {
        val json =
            """
            {
              "sha": "abc",
              "url": "https://api.github.com/repos/o/r/git/trees/abc",
              "tree": [
                { "path": "cv", "mode": "040000", "type": "tree", "sha": "1", "url": "u" },
                { "path": "cv/1.0.0/atom.toml", "mode": "100644", "type": "blob", "sha": "2", "size": 120, "url": "u" }
              ],
              "truncated": false
            }
            """.trimIndent()
        val decoded = GitHubTree.decode(json)
        assertEquals("abc", decoded.sha)
        assertEquals(2, decoded.tree.size)
        assertEquals(120L, decoded.tree[1].size)
        assertEquals(listOf("cv/1.0.0/atom.toml"), GitHubTreeIndex(decoded).filesUnder("cv/1.0.0"))
    }
}
