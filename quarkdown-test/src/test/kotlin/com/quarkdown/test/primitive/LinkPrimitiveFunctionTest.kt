package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to Markdown links.
 */
class LinkPrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("See [here](https://example.com).") {
            assertEquals(
                "<p>See <a href=\"https://example.com\">here</a>.</p>",
                it,
            )
        }
    }

    @Test
    fun `extension styles every link`() {
        execute(
            """
            .extend {link}
                .super foreground:{red}

            See [here](https://example.com).
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <a href=\"https://example.com\" style=\"color: rgba(255, 0, 0, 1.0);\">here</a>.</p>",
                it,
            )
        }
    }

    @Test
    fun `extension body with trailing text stays inline`() {
        execute(
            """
            .extend {link}
                .super (external)

            See the [docs](https://example.com)
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See the <a href=\"https://example.com\">docs</a> (external)</p>",
                it,
            )
        }
    }

    @Test
    fun `extension can match by url`() {
        execute(
            """
            .extend {link} where:{url: .url::startswith {https://match}}
                .super foreground:{red}

            [Match](https://match.com) and [No match](https://other.com)
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><a href=\"https://match.com\" style=\"color: rgba(255, 0, 0, 1.0);\">Match</a> " +
                    "and <a href=\"https://other.com\">No match</a></p>",
                it,
            )
        }
    }
}
