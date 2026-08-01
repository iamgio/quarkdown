package com.quarkdown.rendering.markdown

import com.quarkdown.core.attachMockPipeline
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.rendering.markdown.post.GfmPostRenderer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [GfmPostRenderer].
 */
class GfmPostRendererTest {
    private fun newContext() = MutableContext(QuarkdownFlavor)

    @Test
    fun `resource generation`() {
        val postRenderer = GfmPostRenderer(newContext())
        val resources = postRenderer.generateResources("# Hello\n\n")
        val resource = resources.single()
        assertIs<TextOutputArtifact>(resource)
        assertEquals("# Hello", resource.content)
        assertEquals(ArtifactType.MARKDOWN, resource.type)
    }

    @Test
    fun `single resource wrapping`() {
        val postRenderer = GfmPostRenderer(newContext())
        val resource =
            postRenderer.wrapResources(
                name = "Hello",
                resources =
                    setOf(
                        TextOutputArtifact(
                            name = "output",
                            content = "Content",
                            type = ArtifactType.MARKDOWN,
                        ),
                    ),
            )
        assertIs<TextOutputArtifact>(resource)
        assertEquals("Hello", resource.name)
    }

    @Test
    fun `multiple resource wrapping`() {
        val postRenderer = GfmPostRenderer(newContext())
        val resource =
            postRenderer.wrapResources(
                name = "Group",
                resources =
                    setOf(
                        TextOutputArtifact(
                            name = "output1",
                            content = "Content 1",
                            type = ArtifactType.MARKDOWN,
                        ),
                        TextOutputArtifact(
                            name = "output2",
                            content = "Content 2",
                            type = ArtifactType.MARKDOWN,
                        ),
                    ),
            )
        assertEquals("Group", resource.name)
        val group = assertIs<OutputResourceGroup>(resource)
        assertEquals(2, group.resources.size)
    }

    @Test
    fun `local media storage is preferred by default`() {
        val postRenderer = GfmPostRenderer(newContext())
        val options = postRenderer.preferredMediaStorageOptions
        assertEquals(ReadOnlyMediaStorageOptions(enableLocalMediaStorage = true), options)
    }

    @Test
    fun `no media resource when storage is empty`() {
        val postRenderer = GfmPostRenderer(newContext())
        val resources = postRenderer.generateResources("# Hello\n\n")
        assertEquals(1, resources.size)
        assertFalse(resources.any { it.name == MEDIA_SUBDIRECTORY_NAME })
    }

    @Test
    fun `media resource is emitted when storage is populated`() {
        val context = newContext()
        context.attachMockPipeline(
            PipelineOptions(permissions = setOf(Permission.ProjectRead, Permission.GlobalRead, Permission.NetworkAccess)),
        )
        context.options.enableLocalMediaStorage = true
        context.mediaStorage.register("src/test/resources/media/file.txt", workingDirectory = null)

        val postRenderer = GfmPostRenderer(context)
        val resources = postRenderer.generateResources("# Hello\n\n")

        // Markdown artifact + media group.
        assertEquals(2, resources.size)
        val mediaGroup = resources.filterIsInstance<OutputResourceGroup>().single { it.name == MEDIA_SUBDIRECTORY_NAME }
        assertTrue(mediaGroup.resources.isNotEmpty(), "Media group should contain at least one file")
    }
}
