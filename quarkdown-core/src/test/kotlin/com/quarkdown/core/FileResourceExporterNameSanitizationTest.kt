package com.quarkdown.core

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [FileResourceExporter] sanitizing file names.
 */
class FileResourceExporterNameSanitizationTest {
    private val exporter = FileResourceExporter(location = File("."), write = false)

    private fun resource(name: String): OutputResource = TextOutputArtifact(name = name, content = "", type = ArtifactType.AUTO)

    private fun sanitize(name: String): String = resource(name).accept(exporter).name

    @Test
    fun `sane file name`() {
        assertEquals("hello", sanitize("hello"))
    }

    @Test
    fun `special characters`() {
        assertEquals("h-e-l-l-o", sanitize("h/e\\l:l*o"))
    }

    @Test
    fun `consecutivespecial characters`() {
        assertEquals("h-i", sanitize("h::::::i"))
    }

    @Test
    fun `accepted special characters`() {
        assertEquals("file_name-123@abc", sanitize("file_name-123@abc"))
    }

    @Test
    fun spaces() {
        assertEquals("file-name", sanitize("file name"))
    }

    @Test
    fun `leading dot`() {
        assertEquals("-hiddenfile", sanitize(".hiddenfile"))
    }

    @Test
    fun `trailing dot`() {
        assertEquals("file-", sanitize("file."))
    }

    @Test
    fun `dot in the middle`() {
        assertEquals("file.name", sanitize("file.name"))
    }

    @Test
    fun `only special characters`() {
        assertEquals("-", sanitize("/\\:*?\"<>:|"))
    }

    @Test
    fun `multiple leading dots`() {
        assertEquals("-..file", sanitize("...file"))
    }
}
