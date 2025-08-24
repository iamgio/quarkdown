package com.quarkdown.lsp

import com.quarkdown.lsp.cache.DocumentCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for document caching functionality, particularly for function calls.
 */
class DocumentCacheTest {
    @Test
    fun `cache is initially null`() {
        val doc = TextDocument("text")
        assertNull(doc.cache)
    }

    @Test
    fun `compute creates cache with function calls`() {
        val text = ".function {arg}"
        val cache = DocumentCache.compute(TextDocument(text))

        assertNotNull(cache)
        assertTrue(cache.functionCalls.isNotEmpty())
        assertEquals(1, cache.functionCalls.size)

        val functionCall = cache.functionCalls.first()
        assertEquals(0..text.length, functionCall.range)
        assertTrue(functionCall.tokens.any { it.lexeme == "function" })
    }

    @Test
    fun `compute handles multiple function calls`() {
        val text = ".function1 {arg1} .function2 {arg2}"
        val cache = DocumentCache.compute(TextDocument(text))

        assertEquals(2, cache.functionCalls.size)

        val function1 = cache.functionCalls.first()
        val function2 = cache.functionCalls.last()

        assertTrue(function1.tokens.any { it.lexeme == "function1" })
        assertTrue(function2.tokens.any { it.lexeme == "function2" })
    }

    @Test
    fun `compute handles nested function calls`() {
        val text = ".outer {.inner {arg}}"
        val cache = DocumentCache.compute(TextDocument(text))

        assertTrue(cache.functionCalls.size >= 2)

        // Find the outer and inner function calls
        val outerCall =
            cache.functionCalls.find { call ->
                call.tokens.any { it.lexeme == "outer" }
            }
        val innerCall =
            cache.functionCalls.find { call ->
                call.tokens.any { it.lexeme == "inner" }
            }

        assertNotNull(outerCall)
        assertNotNull(innerCall)

        // Verify the inner call is within the range of the outer call
        assertTrue(innerCall.range.first >= outerCall.range.first)
        assertTrue(innerCall.range.last <= outerCall.range.last)
    }

    @Test
    fun `cacheOrCompute returns existing cache if available`() {
        val text = ".function {arg}"
        // Create a real cache using compute
        val initialCache = DocumentCache.compute(TextDocument(text))

        val doc = TextDocument(text, initialCache)
        val retrievedCache = doc.cacheOrCompute

        assertSame(initialCache, retrievedCache)
    }

    @Test
    fun `cacheOrCompute computes new cache if none exists`() {
        val text = ".function {arg}"
        val doc = TextDocument(text)

        val computedCache = doc.cacheOrCompute

        assertNotNull(computedCache)
        assertTrue(computedCache.functionCalls.isNotEmpty())
        assertEquals(1, computedCache.functionCalls.size)
    }

    @Test
    fun `updateCache returns new document with updated cache`() {
        val doc = TextDocument(".function {arg}")
        val newCache = DocumentCache(emptyList())

        val updatedDoc = doc.updateCache(newCache)

        assertSame(newCache, updatedDoc.cache)
    }

    @Test
    fun `invalidateCache returns new document with null cache`() {
        val initialCache = DocumentCache(emptyList())
        val doc = TextDocument(".function {arg}", initialCache)

        val invalidatedDoc = doc.invalidateCache()

        assertNull(invalidatedDoc.cache)
    }

    @Test
    fun `setActive overwrites active document`() {
        val docs = mutableMapOf<String, TextDocument>()

        val key = "key"
        docs[key] = TextDocument(".function {arg}", setActive = { docs[key] = this })

        val doc = docs[key]!!
        val newDoc = doc.updateCache(DocumentCache.compute(doc))
        assertNotNull(newDoc.cache)

        assertNotSame(docs[key], newDoc)

        newDoc.setActive(newDoc)
        assertSame(docs[key], newDoc)
    }
}
