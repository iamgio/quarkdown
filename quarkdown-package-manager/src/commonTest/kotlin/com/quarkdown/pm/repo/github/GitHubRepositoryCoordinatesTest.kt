package com.quarkdown.pm.repo.github

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [GitHubRepositoryCoordinates] URL construction.
 */
class GitHubRepositoryCoordinatesTest {
    private val coordinates = GitHubRepositoryCoordinates(owner = "quarkdown-labs", repository = "atoms")

    @Test
    fun `subtree url lists one directory of the ref recursively`() {
        assertEquals(
            "https://api.github.com/repos/quarkdown-labs/atoms/git/trees/main:cv?recursive=1",
            coordinates.subtreeUrl("cv").toString(),
        )
    }

    @Test
    fun `raw file url targets raw githubusercontent`() {
        assertEquals(
            "https://raw.githubusercontent.com/quarkdown-labs/atoms/main/cv/1.0.0/atom.toml",
            coordinates.rawFileUrl("cv/1.0.0/atom.toml").toString(),
        )
    }

    @Test
    fun `raw file url percent-encodes reserved and non-ascii characters`() {
        assertEquals(
            "https://raw.githubusercontent.com/quarkdown-labs/atoms/main/cv/1.0.0/my%20photo%20%231%20100%25.png",
            coordinates.rawFileUrl("cv/1.0.0/my photo #1 100%.png").toString(),
        )
        assertEquals(
            "https://raw.githubusercontent.com/quarkdown-labs/atoms/main/cv/1.0.0/r%C3%A9sum%C3%A9.qd",
            coordinates.rawFileUrl("cv/1.0.0/résumé.qd").toString(),
        )
        assertEquals(
            "https://raw.githubusercontent.com/quarkdown-labs/atoms/main/cv/1.0.0/a%3Fb.qd",
            coordinates.rawFileUrl("cv/1.0.0/a?b.qd").toString(),
        )
    }

    @Test
    fun `custom ref is used in both urls`() {
        val dev = coordinates.copy(ref = "dev")
        assertEquals("https://api.github.com/repos/quarkdown-labs/atoms/git/trees/dev:cv?recursive=1", dev.subtreeUrl("cv").toString())
        assertEquals("https://raw.githubusercontent.com/quarkdown-labs/atoms/dev/x", dev.rawFileUrl("x").toString())
    }
}
