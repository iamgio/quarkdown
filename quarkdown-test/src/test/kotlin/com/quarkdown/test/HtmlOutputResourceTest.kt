package com.quarkdown.test

import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubResources
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

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
                assertContains(resources, MEDIA_SUBDIRECTORY_NAME)
            },
        ) {}
    }

    private fun getSearchIndexOutputResource(group: OutputResource?): TextOutputArtifact {
        val resources = getSubResources(group)
        return resources.filterIsInstance<TextOutputArtifact>().first { it.name == "search-index" }
    }

    private fun getSearchIndexInternalResource(name: String): String =
        javaClass
            .getResource("/data/search-index/$name.json")!!
            .readText()
            .let { Json.parseToJsonElement(it).toString() }

    @Test
    fun `with search index, no headings, no metadata`() {
        execute(
            """
            .doctype {docs}
            
            [1](subdoc/simple-1.qd)
            [2](subdoc/simple-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-no-headings-no-metadata"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    @Test
    fun `with search index, no headings, with metadata`() {
        execute(
            """
            .docname {Test}
            .doctype {docs}
            
            [1](subdoc/simple-1.qd)
            [2](subdoc/metadata.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-no-headings-with-metadata"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }

    @Test
    fun `with search index, with headings`() {
        execute(
            """
            .docname {Test}
            .doctype {docs}
            
            [1](subdoc/headings-1.qd)
            [1](subdoc/headings-2.qd)
            """.trimIndent(),
            outputResourceHook = { group ->
                assertEquals(
                    getSearchIndexInternalResource("search-index-with-headings"),
                    getSearchIndexOutputResource(group).content,
                )
            },
        ) {}
    }
}
