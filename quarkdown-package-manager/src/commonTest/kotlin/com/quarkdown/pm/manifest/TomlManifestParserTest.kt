package com.quarkdown.pm.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests for [TomlManifestParser].
 */
class TomlManifestParserTest {
    private val parser = TomlManifestParser

    @Test
    fun `parses full manifest`() {
        val manifest =
            parser.parse(
                """
                [atom]
                name = "cv"
                version = "1.0.0"
                entrypoint = "index.qd"
                min-quarkdown = "2.6.4"
                authors = [ "John Doe <https://example.com>" ]
                license = "Apache-2.0"
                description = "A clean CV template"
                categories = ["cv", "resume"]
                repository = "https://github.com/example/cv"
                """.trimIndent(),
            )
        val atom = manifest.atom
        assertEquals("cv", atom.name)
        assertEquals("1.0.0", atom.version)
        assertEquals("index.qd", atom.entrypoint)
        assertEquals("2.6.4", atom.minQuarkdownVersion)
        assertEquals(listOf("John Doe <https://example.com>"), atom.authors)
        assertEquals("Apache-2.0", atom.license)
        assertEquals("A clean CV template", atom.description)
        assertEquals(listOf("cv", "resume"), atom.categories)
        assertEquals("https://github.com/example/cv", atom.repository)
    }

    @Test
    fun `applies defaults to minimal manifest`() {
        val atom =
            parser
                .parse(
                    """
                    [atom]
                    name = "cv"
                    version = "1.0.0"
                    entrypoint = "main.qd"
                    """.trimIndent(),
                ).atom
        assertEquals("main.qd", atom.entrypoint)
        assertNull(atom.description)
        assertNull(atom.minQuarkdownVersion)
        assertNull(atom.repository)
        assertNull(atom.license)
        assertEquals(emptyList(), atom.authors)
        assertEquals(emptyList(), atom.categories)
    }

    @Test
    fun `ignores unknown keys`() {
        val atom =
            parser
                .parse(
                    """
                    [atom]
                    name = "cv"
                    version = "1.0.0"
                    entrypoint = "main.qd"
                    homepage = "https://example.com"

                    [future]
                    key = 1
                    """.trimIndent(),
                ).atom
        assertEquals("cv", atom.name)
    }

    @Test
    fun `fails on missing version`() {
        assertFailsWith<ManifestParseException> {
            parser.parse(
                """
                [atom]
                name = "cv"
                entrypoint = "main.qd"
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `fails on missing entrypoint`() {
        assertFailsWith<ManifestParseException> {
            parser.parse(
                """
                [atom]
                name = "cv"
                version = "1.0.0"
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `fails on malformed toml`() {
        assertFailsWith<ManifestParseException> {
            parser.parse("[atom\nname = ")
        }
    }

    @Test
    fun `fails on missing atom table`() {
        assertFailsWith<ManifestParseException> {
            parser.parse("name = \"cv\"")
        }
    }

    @Test
    fun `parses template table`() {
        val template =
            parser
                .parse(
                    """
                    [atom]
                    name = "cv"
                    version = "1.0.0"
                    entrypoint = "main.qd"

                    [template]
                    doctype = "paged"
                    content = "template.qd"
                    additional-files = ["assets/photo.png", "sections/experience.qd"]
                    """.trimIndent(),
                ).template
        assertEquals("paged", template?.doctype)
        assertEquals("template.qd", template?.content)
        assertEquals(listOf("assets/photo.png", "sections/experience.qd"), template?.additionalFiles)
    }

    @Test
    fun `template table defaults to content only`() {
        val template =
            parser
                .parse(
                    """
                    [atom]
                    name = "cv"
                    version = "1.0.0"
                    entrypoint = "main.qd"

                    [template]
                    content = "template.qd"
                    """.trimIndent(),
                ).template
        assertEquals("template.qd", template?.content)
        assertNull(template?.doctype)
        assertEquals(emptyList(), template?.additionalFiles)
    }

    @Test
    fun `manifest without template table has no template`() {
        assertNull(
            parser
                .parse(
                    """
                    [atom]
                    name = "cv"
                    version = "1.0.0"
                    entrypoint = "main.qd"
                    """.trimIndent(),
                ).template,
        )
    }

    @Test
    fun `template table without content is rejected`() {
        assertFailsWith<ManifestParseException> {
            parser.parse(
                """
                [atom]
                name = "cv"
                version = "1.0.0"
                entrypoint = "main.qd"

                [template]
                doctype = "paged"
                """.trimIndent(),
            )
        }
    }
}
