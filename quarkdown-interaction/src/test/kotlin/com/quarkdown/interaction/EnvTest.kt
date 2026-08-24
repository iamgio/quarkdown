package com.quarkdown.interaction

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [Env]'s derivation of Node's module resolution path.
 *
 * Quarkdown derives `NODE_PATH` itself instead of relying on a launcher script to export it,
 * so that PDF export works when the CLI is a native binary with no wrapper script.
 */
class EnvTest {
    private fun nodeModulesUnder(prefix: String) = File(prefix, "node_modules").path

    @Test
    fun `explicit NODE_PATH takes precedence over the npm prefix`() {
        assertEquals(
            "/explicit/path",
            Env.nodePathFrom(explicitNodePath = "/explicit/path", npmPrefix = "/some/prefix"),
        )
    }

    @Test
    fun `node path is derived from the npm prefix when NODE_PATH is unset`() {
        assertEquals(
            nodeModulesUnder("/some/prefix"),
            Env.nodePathFrom(explicitNodePath = null, npmPrefix = "/some/prefix"),
        )
    }

    @Test
    fun `node path is null when neither is set`() {
        assertNull(Env.nodePathFrom(explicitNodePath = null, npmPrefix = null))
    }

    @Test
    fun `blank values are treated as unset`() {
        assertEquals(
            nodeModulesUnder("/some/prefix"),
            Env.nodePathFrom(explicitNodePath = "  ", npmPrefix = "/some/prefix"),
        )
        assertNull(Env.nodePathFrom(explicitNodePath = "", npmPrefix = ""))
    }
}
