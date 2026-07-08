package com.quarkdown.processor.integration

import com.quarkdown.processor.fixtures.Generics
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/** Type rendering in wrapper signatures: generic, nullable, multi-argument, nested. */
class TypeRenderingTest {
    @Test
    fun `wrapper is registered under its source name regardless of parameter shape`() {
        val exported = Generics.Module.map { it.name }.toSet()
        assertEquals(setOf("genericTypes"), exported)
    }

    @Test
    fun `generic, nullable, multi-argument and nested types all render fully qualified`() {
        val source = GeneratedFiles.sourceOf("Generics")

        assertContains(source, "`items`: kotlin.collections.List<kotlin.String>")
        assertContains(source, "`nullable`: kotlin.String?")
        assertContains(source, "`mapping`: kotlin.collections.Map<kotlin.String, kotlin.Int>")
        assertContains(source, "`nested`: kotlin.collections.List<kotlin.collections.List<kotlin.String>>")
    }
}
