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
                "This is a <em>TEST</em> document for <em>TESTING</em>" +
                    " some <em>TEXT</em> matching features",
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
                "This is a <em>TEST</em> for <em>TESTING</em> some <em>TEXT</em> features",
                it,
            )
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
