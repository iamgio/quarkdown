package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class UtilitiesTest {
    @Test
    fun `show quarkdown source and result`() {
        val input =
            """
                # Hello
                
                This is **Quarkdown**... .text {!} size:{huge}
                
                .container
                    Nice!
            """

        val codeOutput =
            "<hr />" +
                "<pre><code class=\"language-markdown\">\n${input.trimIndent()}</code></pre>"

        val contentOutput =
            "<div class=\"page-break\" data-hidden=\"\">" +
                "</div><h1>Hello</h1><p>This is <strong>Quarkdown</strong>&hellip; " +
                "<span class=\"size-huge\">!</span></p><div class=\"container\">" +
                "<p>Nice!</p>" +
                "</div>"

        execute(
            """
            .function {sourceresult}
                source:
                ---
                .code {markdown}
                    .source

                .source
            
            .sourceresult
                $input
            """.trimIndent(),
        ) {
            assertEquals(codeOutput + contentOutput, it)
        }

        execute(
            """
            .function {sourceresult}
                shrinkvertical? source:
                ---
                .code {markdown}
                    .source

                .var {output}
                    .container alignment:{center} fullwidth:{yes}
                        .source

                .output
                
            .sourceresult
                $input
            """.trimIndent(),
        ) {
            assertEquals(
                codeOutput +
                    "<div class=\"container fullwidth\" style=\"text-align: center; justify-items: center;\">$contentOutput</div>",
                it,
            )
        }

        execute(
            """
            .function {sourceresult}
                shrinkvertical? source:
                ---
                .code {markdown}
                    .source

                .var {output}
                    .container
                        =
                    
                    .var {voffset} {-50}
                
                    .container alignment:{center} margin:{.voffset::takeif {.shrinkvertical::otherwise {no}}::otherwise {0} 0 0 0} fullwidth:{yes}
                        .source

                .output
                
            .sourceresult
                $input
            
            .sourceresult shrinkvertical:{no}
                $input
            
            .sourceresult shrinkvertical:{yes}
                $input
            """.trimIndent(),
        ) {
            fun output(shrinkVertical: Boolean) =
                codeOutput +
                    "<div class=\"container\"><p>=</p></div>" +
                    "<div class=\"container fullwidth\" style=\"" +
                    "margin: ${if (shrinkVertical) "-50.0px" else "0.0px"} 0.0px 0.0px 0.0px; text-align: center; justify-items: center;" +
                    "\">" +
                    contentOutput +
                    "</div>"

            assertEquals(
                output(false) +
                    output(false) +
                    output(true),
                it,
            )
        }

        execute(
            """
            .function {sourceresult}
                animated? source:
                ---
                .code {markdown}
                    .source

                .var {output}
                    .container
                        =
                
                    .container
                        .source

                .let {.animated::otherwise {no}}
                    .if {.1}
                        .fragment
                            .output
                    .ifnot {.1}
                        .output
                
            .sourceresult
                $input
            
            .sourceresult animated:{no}
                $input
            
            .sourceresult animated:{yes}
                $input
            """.trimIndent(),
        ) {
            fun output(animate: Boolean) =
                codeOutput +
                    (if (animate) "<div class=\"fragment fade-in\">" else "") +
                    "<div class=\"container\"><p>=</p></div>" +
                    "<div class=\"container\">$contentOutput</div>" +
                    if (animate) "</div>" else ""

            assertEquals(
                output(false) +
                    output(false) +
                    output(true),
                it,
            )
        }
    }
}
