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
}
