package com.quarkdown.processor.locale

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the source emitted by [LocaleTableCodeGenerator].
 */
class LocaleTableCodeGeneratorTest {
    private val source = LocaleTableCodeGenerator().buildSource()

    @Test
    fun `declares both tables`() {
        assertContains(source, "package $PACKAGE_NAME")
        assertContains(source, "internal val Languages: NameTable =")
        assertContains(source, "internal val Territories: NameTable =")
    }

    @Test
    fun `contains known entries`() {
        assertContains(source, "\"it\",")
        assertContains(source, "\"Italian\",")
        assertContains(source, "\"US\",")
        assertContains(source, "\"United States\",")
    }

    @Test
    fun `codes are sorted for binary search`() {
        val codes =
            Regex("\"([a-z]{2,3})\",")
                .findAll(source.substringBefore("names ="))
                .map { it.groupValues[1] }
                .toList()
        assertTrue(codes.isNotEmpty())
        assertEquals(codes, codes.sorted())
    }
}
