package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * `@Spread(exclude = [...])`: excluded components are left out of the wrapper signature and
 * KDoc, and the reconstruction falls back to their constructor defaults.
 */
class SpreadExclusionTest {
    private val source by lazy { GeneratedFiles.sourceOf("SpreadExclusion") }

    @Test
    fun `excluded component does not shadow the plain parameter of the same name`() {
        // Only the function's own `alignment: Int` survives; the spread's `alignment: String` is gone.
        assertContains(source, "`alignment`: kotlin.Int")
        assertFalse("`alignment`: kotlin.String" in source, "excluded spread component leaked into the wrapper:\n$source")
    }

    @Test
    fun `non-excluded components are still expanded`() {
        assertContains(source, "`background`: kotlin.String = \"white\"")
    }

    @Test
    fun `reconstruction omits the excluded component so its constructor default applies`() {
        assertContains(
            source,
            "`appearance` = com.quarkdown.processor.fixtures.`Appearance`(`backgroundColor` = `background`)",
        )
    }

    @Test
    fun `excluded component param tag is dropped from the wrapper KDoc`() {
        assertEquals(
            1,
            Regex("@param alignment").findAll(source).count(),
            "only the function's own @param alignment tag should remain:\n$source",
        )
        assertFalse("its tag must not reach the wrapper" in source)
        assertContains(source, "@param background the fill color")
    }
}
