package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.permissions.Permission
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for the `@permission` documentation tag.
 */
class PermissionsTransformerTest : QuarkdocDokkaTest(imports = listOf(Permission::class)) {
    @Test
    fun `no permissions`() {
        test(
            """
            /**
             *
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertFalse("Permissions" in it)
        }
    }

    @Test
    fun `single permission, no description`() {
        test(
            """
            /**
             * @permission [Permission.ProjectRead]
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Permissions")
            assertContains(it, "project-read")
            assertContains(it, "table-row")
        }
    }

    @Test
    fun `single permission, with description`() {
        test(
            """
            /**
             * @permission [Permission.ProjectRead] to read project data
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Permissions")
            assertContains(it, "project-read")
            assertContains(it, """<div class="title">to read project data</div>""")
        }
    }

    @Test
    fun `multiple permissions`() {
        test(
            """
            /**
             * @permission [Permission.ProjectRead] to read project data
             * @permission [Permission.GlobalRead] to read global data
             * @permission [Permission.NetworkAccess] to access the network
             */
            fun func() = Unit
            """.trimIndent(),
            "func",
        ) {
            assertContains(it, "Permissions")
            assertContains(it, "project-read")
            assertContains(it, """<div class="title">to read project data</div>""")
            assertContains(it, "global-read")
            assertContains(it, """<div class="title">to read global data</div>""")
            assertContains(it, "network")
            assertContains(it, """<div class="title">to access the network</div>""")
        }
    }
}
