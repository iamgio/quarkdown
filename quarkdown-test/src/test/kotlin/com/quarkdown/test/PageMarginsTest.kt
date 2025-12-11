package com.quarkdown.test

import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.test.util.execute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for page margins rendering and initialization via [com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer].
 */
class PageMarginsTest {
    @BeforeTest
    fun setUp() {
        PageMarginContentInitializer.resetCounter()
    }

    @Test
    fun `one fixed margin`() {
        execute(
            """
            .pagemargin {topcenter}
                Content
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-top-center\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"top-center\" " +
                    "data-on-left-page=\"top-center\" data-on-right-page=\"top-center\">" +
                    "<p>Content</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>",
                it,
            )
        }
    }

    @Test
    fun `multiple fixed margins`() {
        execute(
            """
            .pagemargin {topleft}
                Left Top
            
            .pagemargin {bottomright}
                Right Bottom
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-top-left\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"top-left\" " +
                    "data-on-left-page=\"top-left\" data-on-right-page=\"top-left\">" +
                    "<p>Left Top</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>" +
                    "<div class=\"page-margin-content page-margin-bottom-right\" " +
                    "data-margin-id=\"page-margin-2\" data-margin-position=\"bottom-right\" " +
                    "data-on-left-page=\"bottom-right\" data-on-right-page=\"bottom-right\">" +
                    "<p>Right Bottom</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-2\" aria-hidden=\"true\" hidden=\"\"></span>",
                it,
            )
        }
    }

    @Test
    fun `one mirror margin`() {
        execute(
            """
            .pagemargin {topinside}
                Content
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-top-inside\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"top-inside\" " +
                    "data-on-left-page=\"top-right\" data-on-right-page=\"top-left\">" +
                    "<p>Content</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>",
                it,
            )
        }
    }

    @Test
    fun `multiple mirror margins`() {
        execute(
            """
            .pagemargin {bottomoutside}
                Outside Bottom
            
            .pagemargin {topinside}
                Inside Top
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-bottom-outside\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"bottom-outside\" " +
                    "data-on-left-page=\"bottom-left\" data-on-right-page=\"bottom-right\">" +
                    "<p>Outside Bottom</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>" +
                    "<div class=\"page-margin-content page-margin-top-inside\" " +
                    "data-margin-id=\"page-margin-2\" data-margin-position=\"top-inside\" " +
                    "data-on-left-page=\"top-right\" data-on-right-page=\"top-left\">" +
                    "<p>Inside Top</p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-2\" aria-hidden=\"true\" hidden=\"\"></span>",
                it,
            )
        }
    }
}
