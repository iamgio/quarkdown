package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the `.match` stdlib function in real-pipeline scenarios.
 */
class MatchTest {
    @Test
    fun `match at block level`() {
        execute(
            """
            .match {This is a test document for testing some text matching features} {te[xs]t(ing)?}
                match:
                *.match::uppercase*
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>This is a <em>TEST</em> document for <em>TESTING</em>" +
                    " some <em>TEXT</em> matching features</p>",
                it,
            )
        }
    }

    @Test
    fun `match in extend body without super`() {
        execute(
            """
            .extend {heading}
                content:
                .content::match {te[xs]t(ing)?}
                    *.1::uppercase*

            #### This is a test for testing some text features
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>This is a <em>TEST</em> for <em>TESTING</em> some <em>TEXT</em> features</p>",
                it,
            )
        }
    }

    @Test
    fun `match lambda body returning inline NodeValue via text call`() {
        execute(
            """
            .match {Quarkdown takes its name from quarks} pattern:{[Qq]uark(down|s)?}
                .text {.1} decoration:{underline}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><span style=\"text-decoration: underline;\">Quarkdown</span> takes its name from " +
                    "<span style=\"text-decoration: underline;\">quarks</span></p>",
                it,
            )
        }
    }

    @Test
    fun `match lambda body returning inline NodeValue via chained text call`() {
        execute(
            """
            .match {Quarkdown is a tool} {Quarkdown}
                .1::text
            """.trimIndent(),
        ) {
            assertEquals("<p><span>Quarkdown</span> is a tool</p>", it)
        }
    }

    @Test
    fun `match in paragraph extension with chained inline call`() {
        execute(
            """
            .extend {paragraph}
                content:
                .content::match {Quarkdown}
                    .1::text

            Quarkdown is a tool.
            """.trimIndent(),
        ) {
            assertEquals("<p><span>Quarkdown</span> is a tool.</p>", it)
        }
    }

    @Test
    fun `match inside heading extension`() {
        execute(
            """
            .extend {heading}
                content:
                .super
                    .content::match {te[xs]t(ing)?}
                        *.1::uppercase*

            #### This is a test document for testing some text matching features
            """.trimIndent(),
        ) {
            assertEquals(
                "<h4>This is a <em>TEST</em> document for <em>TESTING</em>" +
                    " some <em>TEXT</em> matching features</h4>",
                it,
            )
        }
    }
}
