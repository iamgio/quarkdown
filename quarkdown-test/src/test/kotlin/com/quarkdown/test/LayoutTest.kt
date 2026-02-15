package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for elements and functions that compose complex layouts.
 */
class LayoutTest {
    @Test
    fun `node mapping`() {
        // Function is a block
        execute(
            """
            ## Title
            
            .libexists {stdlib}
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p><input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
        }

        // Function is inline
        execute(
            """
            ## Title
            
            Text .libexists {stdlib}
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p>Text <input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
        }
    }

    @Test
    fun stacks() {
        execute(
            """
            .row
                Hello 1
                Hello 2
                
                Hello 3
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: flex-start; align-items: center;\" class=\"stack stack-row\">" +
                    "<p>Hello 1\nHello 2</p><p>Hello 3</p>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .column alignment:{spacebetween} cross:{start} gap:{1cm}
                Hello 1
                
                ## Hello 2
                
                    Hello 3
                    
                .box {Hello 4} type:{tip}
                    Hello 5
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: space-between; align-items: flex-start; row-gap: 1.0cm;\" class=\"stack stack-column\">" +
                    "<p>Hello 1</p>" +
                    "<h2>Hello 2</h2>" +
                    "<pre><code>Hello 3</code></pre>" +
                    "<div class=\"box tip\"><header><h4>Hello 4</h4></header><div class=\"box-content\"><p>Hello 5</p></div></div>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .row alignment:{center} cross:{center} gap:{200px}
                .column cross:{end}
                    ## Quarkdown
                    A cool language

                .column gap:{1cm}
                    .clip {circle}
                        ![](img1.png)

                    .clip {circle}
                        ![](img2.png)

                    .clip {circle}
                        ![](img3.png)

                **[GitHub](https://github.com/iamgio/quarkdown)**
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: center; align-items: center; column-gap: 200.0px;\" class=\"stack stack-row\">" +
                    "<div style=\"justify-content: flex-start; align-items: flex-end;\" class=\"stack stack-column\">" +
                    "<h2>Quarkdown</h2><p>A cool language</p>" +
                    "</div>" +
                    "<div style=\"justify-content: flex-start; align-items: center; row-gap: 1.0cm;\" class=\"stack stack-column\">" +
                    "<div class=\"clip clip-circle\"><div class=\"container\">" +
                    "<figure><img src=\"img1.png\" alt=\"\" /></figure>" +
                    "</div></div>" +
                    "<div class=\"clip clip-circle\"><div class=\"container\">" +
                    "<figure><img src=\"img2.png\" alt=\"\" /></figure>" +
                    "</div></div>" +
                    "<div class=\"clip clip-circle\"><div class=\"container\">" +
                    "<figure><img src=\"img3.png\" alt=\"\" /></figure>" +
                    "</div></div>" +
                    "</div>" +
                    "<p><strong><a href=\"https://github.com/iamgio/quarkdown\">GitHub</a></strong></p>" +
                    "</div>",
                it,
            )
        }
    }

    @Test
    fun gridGap() {
        execute(
            """
            .grid columns:{2} rowGap:{1cm} columnGap:{2cm}
                Hello 1
                
                Hello 2
                
                Hello 3
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"grid-template-columns: auto auto; justify-content: center; align-items: center; row-gap: 1.0cm; column-gap: 2.0cm;\" class=\"stack stack-grid\">" +
                        "<p>Hello 1</p>" +
                        "<p>Hello 2</p>" +
                        "<p>Hello 3</p>" +
                        "</div>",
                it,
            )
        }

        execute(
            """
            .grid columns:{2} gap:{1cm} columnGap:{2cm}
                Hello 1
                
                Hello 2
                
                Hello 3
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"grid-template-columns: auto auto; justify-content: center; align-items: center; row-gap: 1.0cm; column-gap: 2.0cm;\" class=\"stack stack-grid\">" +
                        "<p>Hello 1</p>" +
                        "<p>Hello 2</p>" +
                        "<p>Hello 3</p>" +
                        "</div>",
                it,
            )
        }
    }

    @Test
    fun float() {
        execute(
            """
            Hello 1
            
            .float {start}
                ![Quarkdown](img/icon.png)
            
            Hello 2
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello 1</p>" +
                    "<div class=\"container float\" style=\"float: inline-start;\">" +
                    "<figure><img src=\"img/icon.png\" alt=\"Quarkdown\" /></figure>" +
                    "</div>" +
                    "<p>Hello 2</p>",
                it,
            )
        }
    }
}
