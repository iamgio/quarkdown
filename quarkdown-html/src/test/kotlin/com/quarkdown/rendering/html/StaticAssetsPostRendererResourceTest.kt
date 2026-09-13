package com.quarkdown.rendering.html

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
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
    fun `virtual public directory is emitted by reference`() {
        val fs = VirtualFileSystem("/project")
        fs.write("public/robots.txt", "User-agent: *")
        fs.write("public/nested/CNAME", "example.com")

        val resources = mutableSetOf<OutputResource>()
        StaticAssetsPostRendererResource(fs.workingDirectory!!).includeTo(resources, rendered = "")

        val resource = resources.single()
        assertIs<FileReferenceOutputArtifact>(resource)
        assertEquals(".", resource.name)
        assertEquals("User-agent: *", resource.file.resolve("robots.txt").readText())
        assertEquals("example.com", resource.file.resolve("nested/CNAME").readText())
    }

    @Test
    fun `missing public directory emits nothing`() {
        val fs = VirtualFileSystem("/project")
        val resources = mutableSetOf<OutputResource>()
        StaticAssetsPostRendererResource(fs.workingDirectory!!).includeTo(resources, rendered = "")
        assertTrue(resources.isEmpty())
    }
}
