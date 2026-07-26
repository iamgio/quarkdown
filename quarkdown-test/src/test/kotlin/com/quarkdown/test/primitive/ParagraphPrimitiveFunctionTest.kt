package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to Markdown paragraphs.
 */
class ParagraphPrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
        execute("Hello") {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun `extension wraps every paragraph`() {
        execute(
            """
            .extend {paragraph}
                .container
                    .super

            Hello
            """.trimIndent(),
        ) {
            assertEquals("<div class=\"container\"><p>Hello</p></div>", it)
        }
    }

    @Test
    fun `content can be matched`() {
        execute(
            """
            .extend {paragraph} where:{content: .content::equals {Hello}}
                .super foreground:{blue} background:{white}
            
            Hello
            
            Hi
            """.trimIndent(),
        ) {
            assertEquals(
                "<p style=\"color: rgba(0, 0, 255, 1.0); background-color: rgba(255, 255, 255, 1.0);\">Hello</p>" +
                    "<p>Hi</p>",
                it,
            )
        }
    }

    @Test
    fun `extension body returning inline content stays wrapped in paragraph`() {
        execute(
            """
            .extend {paragraph}
                content:
                .content::match {C}
                    *.1*

            A

            B

            C
            """.trimIndent(),
        ) {
            assertEquals("<p>A</p><p>B</p><p><em>C</em></p>", it)
        }
    }

    @Test
    fun `extension body match with nested text call`() {
        execute(
            """
            .extend {paragraph}
                content:
                .content::match {[Qq]uark(down|s)?}
                    .text {.1} decoration:{underline}

            Quarkdown takes its name from quarks
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
    fun `content can be matched against pattern`() {
        execute(
            """
            .extend {paragraph}
                content:
                .super
                    .content::match {[Qq]uark(down|s)?}
                        **.1**

            Quarkdown takes its name from quarks
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Quarkdown</strong> takes its name from <strong>quarks</strong></p>", it)
        }
    }

    @Test
    fun `chained extensions match complementary patterns on the same content`() {
        execute(
            """
            .extend {paragraph}
                content:
                .super
                    .content::match {A}
                        .1::text color:{blue}

            .extend {paragraph}
                content:
                .super
                    .content::match {B}
                        .1::text color:{red}

            A B C
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><span style=\"color: rgba(0, 0, 255, 1.0);\">A</span> " +
                    "<span style=\"color: rgba(255, 0, 0, 1.0);\">B</span> C</p>",
                it,
            )
        }
    }

    @Test
    fun `chained extensions preserve semantic inline wrappers around a re-matched substring`() {
        execute(
            """
            .extend {paragraph}
                content:
                .super
                    .content::match {A}
                        *.1*

            .extend {paragraph}
                content:
                .super
                    .content::match {A}
                        **.1**

            A B C
            """.trimIndent(),
        ) {
            assertEquals("<p><em><strong>A</strong></em> B C</p>", it)
        }
    }
}
