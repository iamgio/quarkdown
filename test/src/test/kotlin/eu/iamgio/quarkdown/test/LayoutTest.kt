package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
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
                "<div style=\"justify-content: space-between; align-items: flex-start; gap: 1.0cm;\" class=\"stack stack-column\">" +
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
                "<div style=\"justify-content: center; align-items: center; gap: 200.0px;\" class=\"stack stack-row\">" +
                    "<div style=\"justify-content: flex-start; align-items: flex-end;\" class=\"stack stack-column\">" +
                    "<h2>Quarkdown</h2><p>A cool language</p>" +
                    "</div>" +
                    "<div style=\"justify-content: flex-start; align-items: center; gap: 1.0cm;\" class=\"stack stack-column\">" +
                    "<div class=\"clip clip-circle\">" +
                    "<figure><img src=\"img1.png\" alt=\"\" /></figure>" +
                    "</div>" +
                    "<div class=\"clip clip-circle\">" +
                    "<figure><img src=\"img2.png\" alt=\"\" /></figure>" +
                    "</div>" +
                    "<div class=\"clip clip-circle\">" +
                    "<figure><img src=\"img3.png\" alt=\"\" /></figure>" +
                    "</div>" +
                    "</div>" +
                    "<p><strong><a href=\"https://github.com/iamgio/quarkdown\">GitHub</a></strong></p>" +
                    "</div>",
                it,
            )
        }
    }

    @Test
    fun boxes() {
        execute(".box {Hello} \n\tHello, **world**!") {
            assertEquals(
                "<div class=\"box callout\"><header><h4>Hello</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, <strong>world</strong>!</p></div></div>",
                it,
            )
        }

        execute(".box {Hello} type:{tip}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box tip\"><header><h4>Hello</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        execute(".box {Hello, *world*} type:{warning}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box warning\"><header><h4>Hello, <em>world</em></h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        execute(".box type:{error}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box error\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        // Localized title.
        execute(
            """
            .doclang {english}
            .box type:{error}
              Hello, world!
            
            .box type:{tip}
               Hello, world!
               
            .box
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"box error\"><header><h4>Error</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>" +
                    "<div class=\"box tip\"><header><h4>Tip</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>" +
                    "<div class=\"box callout\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        // Unsupported localization.
        execute(
            """
            .doclang {japanese}
            .box type:{warning}
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"box warning\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }
    }
}
