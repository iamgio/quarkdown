package com.quarkdown.test

import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for generation of HTML output resources.
 */
class HtmlOutputResourceTest {
    @Test
    fun `regular output with index, theme and script dirs`() {
        execute(
            source = "",
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, "index")
                assertContains(resources, "theme")
                assertContains(resources, "script")
            },
        ) {}
    }

    @Test
    fun `with media`() {
        execute(
            source = "![](img/icon.png)",
            enableMediaStorage = true,
            outputResourceHook = { group ->
                val resources = getSubResources(group).map { it.name }
                assertContains(resources, "media")
            },
        ) {}
    }
}
