package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.MultipleFunctions
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/** Modules containing more than one `@QFunction`. */
class MultipleFunctionsTest {
    @Test
    fun `every @QFunction in the file is exported`() {
        val exported = MultipleFunctions.Module.map { it.name }.toSet()
        assertEquals(setOf("alpha", "bravo", "charlie"), exported)
    }

    @Test
    fun `each function gets its own wrapper and its own moduleOf entry`() {
        val source = GeneratedFiles.sourceOf("MultipleFunctions")

        assertContains(source, "public fun `alpha`(): com.quarkdown.core.function.value.VoidValue")
        assertContains(source, "public fun `bravo`(`text`: kotlin.String)")
        assertContains(source, "public fun `charlie`(`a`: kotlin.Int, `b`: kotlin.Int)")

        assertContains(source, "this::`alpha`,")
        assertContains(source, "this::`bravo`,")
        assertContains(source, "this::`charlie`,")
    }
}
