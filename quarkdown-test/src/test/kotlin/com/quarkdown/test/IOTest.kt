package com.quarkdown.test

import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for IO functions.
 */
class IOTest {
    @Test
    fun `read file`() {
        execute(".read {code.txt}") {
            assertEquals(
                "<p>Line 1\nLine 2\n\nLine 3</p>",
                it,
            )
        }
    }

    @Test
    fun `list files`() {
        val files = File(DATA_FOLDER, "include").listFiles()!!
        execute(".listfiles {include} sortby:{name} order:{descending} fullpath:{no}") {
            assertTrue(it.startsWith("<ol>"))
            files.forEach { file ->
                assertContains(it, "<li><p>${file.name}</p></li>")
            }
            assertTrue(it.endsWith("</ol>"))
        }
    }

    @Test
    fun `json object lookup`() {
        execute(".json {json/config.json}::get {database}::get {host}") {
            assertEquals("<p>localhost</p>", it)
        }
    }

    @Test
    fun `json object scoping`() {
        execute(
            """
            .json {json/config.json}::get {database}::let
                .1::get {host}:.1::get {port}
            """.trimIndent(),
        ) {
            assertEquals("<p>localhost:5432</p>", it)
        }
    }

    @Test
    fun `json array iteration`() {
        execute(
            """
            .json {json/people.json}::foreach
                .1::get {name} is from .1::get {address}::get {country}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Alice is from USA</p><p>Bob is from Italy</p>",
                it,
            )
        }
    }
}
