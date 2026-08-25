package com.quarkdown.rendering.html

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.resources.StaticAssetsPostRendererResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [StaticAssetsPostRendererResource] on a virtual file system.
 */
class StaticAssetsPostRendererResourceTest {
    @Test
    fun `virtual public directory is emitted as a resource group`() {
        val fs = VirtualFileSystem("/project")
        fs.write("public/robots.txt", "User-agent: *")
        fs.write("public/nested/CNAME", "example.com")

        val resources = mutableSetOf<OutputResource>()
        StaticAssetsPostRendererResource(fs.workingDirectory!!).includeTo(resources, rendered = "")

        val group = resources.single()
        assertIs<OutputResourceGroup>(group)
        assertEquals(".", group.name)
        val robots = group.resources.filterIsInstance<BinaryOutputArtifact>().single()
        assertEquals("robots.txt", robots.name)
        val nested = group.resources.filterIsInstance<OutputResourceGroup>().single()
        assertEquals("nested", nested.name)
        assertTrue(nested.resources.filterIsInstance<BinaryOutputArtifact>().any { it.name == "CNAME" })
    }

    @Test
    fun `missing public directory emits nothing`() {
        val fs = VirtualFileSystem("/project")
        val resources = mutableSetOf<OutputResource>()
        StaticAssetsPostRendererResource(fs.workingDirectory!!).includeTo(resources, rendered = "")
        assertTrue(resources.isEmpty())
    }
}
