package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubdocumentResource
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for navigation sidebar generation via [com.quarkdown.rendering.html.node.SidebarRenderer].
 */
class SidebarTest {
    @Test
    fun generation() {
        execute(
            """
            # Title 1
            
            ## Subtitle 1.1
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
            outputResourceHook = { group ->
                val indexResource = getSubdocumentResource(group, Subdocument.Root, this)

                val expectedSidebar =
                    "<ol>" +
                        "<li data-target-id=\"title-1\" data-depth=\"1\"><a href=\"#title-1\">Title 1</a>" +
                        "<ol>" +
                        "<li data-target-id=\"subtitle-11\" data-depth=\"2\"><a href=\"#subtitle-11\">Subtitle 1.1</a></li>" +
                        "</ol>" +
                        "</li>" +
                        "</ol>"

                assertContains(indexResource.content, expectedSidebar)
                assertContains(
                    indexResource.content,
                    Regex("<template id=\"sidebar-template\">\\s*<nav class=\"sidebar\" role=\"doc-toc\">\\s*<ol>"),
                )
            },
        ) {}
    }
}
