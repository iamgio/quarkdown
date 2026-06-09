package com.quarkdown.lsp.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression tests for [CacheableFunctionCatalogue]'s concurrent population path.
 * The catalogue is a JVM-global singleton, so each test resets its internal map via reflection
 * before and after running to prevent cross-test contamination.
 */
class CacheableFunctionCatalogueTest {
    private val docsDir = File("/nonexistent/docs")

    @BeforeTest
    fun setup() {
        clearCatalogue()
        mockkObject(CacheableFunctionCatalogue, recordPrivateCalls = true)
    }

    @AfterTest
    fun teardown() {
        unmockkObject(CacheableFunctionCatalogue)
        clearCatalogue()
    }

    @Test
    fun `concurrent storeCatalogue invokes walk exactly once`() {
        val expected = setOf(mockk<DocumentedFunction>())
        every { CacheableFunctionCatalogue["walk"](docsDir) } returns expected

        val threadCount = 32
        val startGate = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threadCount)
        try {
            repeat(threadCount) {
                pool.submit {
                    startGate.await()
                    CacheableFunctionCatalogue.storeCatalogue(docsDir)
                }
            }
            startGate.countDown()
            pool.shutdown()
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS))
        } finally {
            pool.shutdownNow()
        }

        verify(exactly = 1) { CacheableFunctionCatalogue["walk"](docsDir) }
        assertEquals(1, catalogueMap().size)
        assertEquals(expected, catalogueMap()[docsDir])
    }

    /**
     * Asserts that an empty walk result is not cached, so subsequent calls keep retrying until
     * a non-empty result is produced.
     */
    @Test
    fun `empty walk result is not cached and triggers retry on next call`() {
        val empty = emptySet<DocumentedFunction>()
        val populated = setOf(mockk<DocumentedFunction>())

        every { CacheableFunctionCatalogue["walk"](docsDir) } returnsMany listOf(empty, empty, populated)

        CacheableFunctionCatalogue.storeCatalogue(docsDir)
        assertTrue(catalogueMap().isEmpty(), "first empty walk must not populate the cache")

        CacheableFunctionCatalogue.storeCatalogue(docsDir)
        assertTrue(catalogueMap().isEmpty(), "second empty walk must not populate the cache")
        verify(exactly = 2) { CacheableFunctionCatalogue["walk"](docsDir) }

        CacheableFunctionCatalogue.storeCatalogue(docsDir)
        assertEquals(populated, catalogueMap()[docsDir], "non-empty walk must populate the cache")

        // Once populated, further calls must short-circuit without walking again.
        CacheableFunctionCatalogue.storeCatalogue(docsDir)
        verify(exactly = 3) { CacheableFunctionCatalogue["walk"](docsDir) }
    }

    private fun catalogueMap(): MutableMap<File, Set<DocumentedFunction>> {
        val field = CacheableFunctionCatalogue::class.java.getDeclaredField("catalogue")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(CacheableFunctionCatalogue) as MutableMap<File, Set<DocumentedFunction>>
    }

    private fun clearCatalogue() {
        catalogueMap().clear()
    }
}
