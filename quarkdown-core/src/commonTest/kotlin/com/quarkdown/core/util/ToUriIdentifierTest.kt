package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [toUriIdentifier].
 *
 * The expectations reflect the JVM behavior, which every target must match:
 * a previous regex shape diverged on Kotlin/Wasm,
 * whose regex engine mishandles negated character classes that mix Unicode properties with literal members.
 */
class ToUriIdentifierTest {
    @Test
    fun `lowercases and replaces whitespace with dashes`() {
        assertEquals("hello-world", "Hello, World!".toUriIdentifier())
        assertEquals("my-title-2", "My title 2".toUriIdentifier())
    }

    @Test
    fun `collapses whitespace runs`() {
        assertEquals("-spaced-out-", "  spaced \t  out ".toUriIdentifier())
    }

    @Test
    fun `keeps non-ASCII letters and digits`() {
        assertEquals("café-au-lait", "Café au lait".toUriIdentifier())
        assertEquals("日本語-テスト", "日本語 テスト".toUriIdentifier())
    }

    @Test
    fun `keeps existing dashes`() {
        assertEquals("pre-built-x-1", "Pre-built x-1".toUriIdentifier())
    }

    @Test
    fun `strips punctuation and symbols`() {
        assertEquals("abc", "a_b.c".toUriIdentifier())
        assertEquals("ab", "a/\\:*\"<>|@#b".toUriIdentifier())
    }

    @Test
    fun `strips characters outside the basic multilingual plane that are not letters`() {
        assertEquals("a--b", "a 😀 b".toUriIdentifier()) // 😀 (U+1F600)
    }

    @Test
    fun `keeps letters outside the basic multilingual plane`() {
        assertEquals("𝒜-x", "𝒜 x".toUriIdentifier()) // 𝒜 (U+1D49C)
    }

    @Test
    fun `handles empty input`() {
        assertEquals("", "".toUriIdentifier())
    }
}
