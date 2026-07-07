package com.quarkdown.processor.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * KDoc emission: the wrapper carries the source function's KDoc with parameter names substituted,
 * link references rewritten, and each `@Spread` parameter's data-class `@param` tags injected in
 * its place. Verifies that downstream Dokka-based documentation sees the wrapper as a self-contained, exported-name-only view of the function.
 */
class KDocEmissionTest {
    private val source by lazy { GeneratedFiles.sourceOf("KDocEmission") }

    @Test
    fun `wrapper carries a KDoc block above the function`() {
        assertContains(source, "\t/**")
        assertContains(source, "\t */")
    }

    @Test
    fun `description text survives verbatim`() {
        assertContains(source, "A documented function whose KDoc must survive substitution.")
    }

    @Test
    fun `param tags on plain parameters are rewritten to the exported name`() {
        // @Name("caption") label -> `caption`
        assertContains(source, "@param caption the label to attach")
        assertFalse("@param label" in source, "source-name @param must be rewritten")
    }

    @Test
    fun `param tags on unrenamed parameters pass through unchanged`() {
        assertContains(source, "@param count how many boxes to render")
    }

    @Test
    fun `link references in description text are rewritten`() {
        // "Reference to [label]" -> "Reference to [caption]"
        assertContains(source, "Reference to [caption]")
        assertFalse("Reference to [label]" in source, "link reference must be rewritten")
    }

    @Test
    fun `link references inside a param description are also rewritten`() {
        assertContains(source, "see [caption] again for the link form")
    }

    @Test
    fun `non-param tags survive`() {
        assertContains(source, "@return the produced value")
        assertContains(source, "@wiki some-page")
    }

    @Test
    fun `spread injection adds the data-class param tags, rewritten`() {
        // @param innerColor -> @param color (via @Name("color") on the data class)
        assertContains(source, "@param color the fill color")
        // @param innerWidth (no rename) -> unchanged name
        assertContains(source, "@param innerWidth the width")
    }

    @Test
    fun `spread injection rewrites link references inside injected tags`() {
        // "See [innerColor] for the reference form" -> "See [color] for the reference form"
        assertContains(source, "See [color] for the reference form")
    }

    @Test
    fun `spread class description text does not bleed into the wrapper KDoc`() {
        assertFalse(
            "Documentation for the [Box] data class" in source,
            "only @param tags of a @Spread class should be injected, not the class description",
        )
        assertFalse(
            "class-level description here must NOT bleed" in source,
            "only @param tags of a @Spread class should be injected, not the class description",
        )
    }

    @Test
    fun `the outer spread parameter itself gets no @param entry`() {
        assertFalse("@param box" in source, "the outer spread parameter must not appear in KDoc")
    }
}
