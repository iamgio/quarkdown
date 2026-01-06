package com.quarkdown.core

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for miscellaneous classes.
 */
class MiscTest {
    @Test
    fun `resource export`() {
        val dir = Files.createTempDirectory("quarkdown-resource-test")
        val exporter = FileResourceExporter(dir.toFile())

        with("Hello, world!".repeat(1000)) {
            assertEquals(
                this,
                TextOutputArtifact("Artifact 1", this, ArtifactType.HTML)
                    .accept(exporter)
                    .also { assertEquals("Artifact-1.html", it.name) }
                    .readText(),
            )
            assertContentEquals(
                this.toByteArray(),
                BinaryOutputArtifact("a/rt*fact::2", this.toByteArray().toList(), ArtifactType.JAVASCRIPT)
                    .accept(exporter)
                    .also { assertEquals("a-rt-fact-2.js", it.name) }
                    .readBytes(),
            )
        }

        with("Quarkdown".repeat(1000)) {
            LazyOutputArtifact("artifact3", { this.toByteArray().toList() }, ArtifactType.CSS)
                .accept(exporter)
                .also { assertEquals("artifact3.css", it.name) }
                .let { file ->
                    assertEquals(this, file.readText())
                    assertContentEquals(this.toByteArray(), file.readBytes())
                }
        }

        LazyOutputArtifact
            .internal(
                resource = "/media/icon.png",
                name = "artif@ct 4.png",
                type = ArtifactType.AUTO,
                referenceClass = this::class,
            ).run {
                assertContentEquals(
                    this::class.java.getResourceAsStream("/media/icon.png")!!.readBytes(),
                    this
                        .accept(exporter)
                        .also { assertEquals("artif@ct-4.png", it.name) }
                        .readBytes(),
                )
            }

        LazyOutputArtifact
            .internalOrNull(
                resource = "nonexisting.png",
                name = "artifact.png",
                type = ArtifactType.AUTO,
                referenceClass = this::class,
            ).let { assertNull(it) }

        val group =
            OutputResourceGroup(
                "Group 1",
                setOf(
                    TextOutputArtifact("Artifact 5", "Hello, world!", ArtifactType.HTML),
                    BinaryOutputArtifact("arti-fact6", "Quarkdown".toByteArray().toList(), ArtifactType.JAVASCRIPT),
                    LazyOutputArtifact("artifact7", { "Quarkdown".toByteArray().toList() }, ArtifactType.CSS),
                    OutputResourceGroup(
                        "Group 2",
                        setOf(
                            TextOutputArtifact("Artifact 8", "Hello, world!", ArtifactType.HTML),
                            BinaryOutputArtifact("art*fact/9", "Quarkdown".toByteArray().toList(), ArtifactType.JAVASCRIPT),
                        ),
                    ),
                    LazyOutputArtifact.internal(
                        referenceClass = this::class,
                        resource = "/media/banner.png",
                        name = "artif@ct 10.png",
                        type = ArtifactType.AUTO,
                    ),
                    BinaryOutputArtifact(
                        "artifact11",
                        "Hello world".repeat(100).toByteArray().toList(),
                        ArtifactType.JAVASCRIPT,
                    ),
                ),
            )

        val groupFile = group.accept(exporter)

        assertTrue(groupFile.isDirectory)
        val files = groupFile.listFiles()!!
        assertEquals(6, files.size)

        assertEquals(1, files.count { it.extension == "html" })
        assertEquals(2, files.count { it.extension == "js" })
        assertEquals(1, files.count { it.extension == "css" })
        assertEquals(1, files.count { it.extension == "png" })

        val subGroup = files.single { it.isDirectory }
        subGroup.listFiles()!!.let { subFiles ->
            assertEquals(2, subFiles.size)
            assertEquals(1, subFiles.count { it.extension == "html" })
            assertEquals(1, subFiles.count { it.extension == "js" })
        }
    }
}
