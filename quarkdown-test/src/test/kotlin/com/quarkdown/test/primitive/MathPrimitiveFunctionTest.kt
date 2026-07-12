package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to Markdown math nodes.
 */
class MathPrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("Hello $ x + 1 $ and $ y $") {
            assertEquals(
                "<p>Hello <formula>x + 1</formula> and <formula>y</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `extension wraps every block math`() {
        execute(
            """
            .extend {math}
                .container
                    .super

            $ x + 1 $
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><formula data-block=\"\">x + 1</formula></div>",
                it,
            )
        }
    }

    @Test
    fun `extension can be matched by block param`() {
        execute(
            """
            .extend {math} where:{block: .block}
                .container
                    .super

            $ y $ is inline

            $ x + 1 $
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><formula>y</formula> is inline</p>" +
                    "<div class=\"container\"><formula data-block=\"\">x + 1</formula></div>",
                it,
            )
        }
    }

    @Test
    fun `chained broad and conditional extensions stack on block, only broad on inline`() {
        execute(
            """
            .extend {math}
                .super foreground:{red}

            .extend {math} where:{block: .block}
                .super fontsize:{large}

            $ a^2 + b^2 = c^2 $

            Inline: $ a^2 + b^2 = c^2 $
            """.trimIndent(),
        ) {
            assertEquals(
                "<formula data-block=\"\" " +
                    "style=\"color: rgba(255, 0, 0, 1.0); font-size: var(--qd-size-large, 1em);\">" +
                    "a^2 + b^2 = c^2</formula>" +
                    "<p>Inline: " +
                    "<formula style=\"color: rgba(255, 0, 0, 1.0);\">a^2 + b^2 = c^2</formula></p>",
                it,
            )
        }
    }
}
