package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to Markdown figures.
 */
class FigurePrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("![Logo](img.png)") {
            assertEquals(
                "<figure><img src=\"img.png\" alt=\"Logo\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `extension wraps every figure`() {
        execute(
            """
            .extend {figure}
                .container
                    .super

            ![Logo](img.png)
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><figure><img src=\"img.png\" alt=\"Logo\" /></figure></div>",
                it,
            )
        }
    }

    @Test
    fun `ref can be matched and conditionally wrapped`() {
        execute(
            """
            .extend {figure} where:{ref: .ref::equals {my-fig}}
                .container
                    .super

            ![Match](match.png) {#my-fig}

            ![No match](nomatch.png)
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><figure id=\"my-fig\"><img src=\"match.png\" alt=\"Match\" /></figure></div>" +
                    "<figure><img src=\"nomatch.png\" alt=\"No match\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `caption can be matched`() {
        execute(
            """
            .extend {figure} where:{caption: .caption::equals {Match}}
                .container
                    .super

            ![Alt](match.png "Match")

            ![Alt](nomatch.png "No match")
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><figure><img src=\"match.png\" alt=\"Alt\" title=\"Match\" />" +
                    "<figcaption class=\"caption-bottom\">Match</figcaption></figure></div>" +
                    "<figure><img src=\"nomatch.png\" alt=\"Alt\" title=\"No match\" />" +
                    "<figcaption class=\"caption-bottom\">No match</figcaption></figure>",
                it,
            )
        }
    }
}
