package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Tests for lambda parameter destructuring inside `.foreach`,
 * which lets each pair/dictionary entry be split into named components.
 */
class DestructuringTest {
    @Test
    fun `destructure dictionary entries into key and value`() {
        execute(
            """
            .var {x}
              .dictionary
                - a: 1
                - b: 2
                - c: 3

            .foreach {.x}
                key value:
                **.key** has value **.value**
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>a</strong> has value <strong>1</strong></p>" +
                    "<p><strong>b</strong> has value <strong>2</strong></p>" +
                    "<p><strong>c</strong> has value <strong>3</strong></p>",
                it,
            )
        }
    }

    @Test
    fun `destructuring fails when parameter count does not match component count`() {
        assertFails {
            execute(
                """
                .var {x}
                  .dictionary
                    - a: 1
                    - b: 2
                    - c: 3

                .foreach {.x}
                    key value extra:
                    **.key** has value **.value**
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `destructure nested dictionary values via the second parameter`() {
        // Authors form a dictionary whose values are themselves dictionaries (info).
        // The lambda destructures each entry into `name` and `info`.
        execute(
            """
            .docauthors
                - Giorgio
                  - email: gio@test.com
                  - country: Italy
                - Mary
                  - country: USA

            .foreach {.docauthors}
                name info:
                .name's country is .info::get {country}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Giorgio&rsquo;s country is Italy</p>" +
                    "<p>Mary&rsquo;s country is USA</p>",
                it,
            )
        }
    }

    @Test
    fun `destructure authors into a grid via a defined layout function`() {
        // Showcases destructuring inside a layout pipeline: each `.docauthors` entry is split
        // into `name` and `info`, then forwarded to a user-defined `.author` function whose
        // own parameters (`name branch email`) are filled positionally from the destructured data.
        execute(
            """
            .docauthors
                - Llion Jones
                  - branch: Google Research
                  - email: llion@google.com
                - Aidan N. Gomez
                  - branch: University of Toronto
                  - email: aidan@cs.toronto.edu
                - Łukasz Kaiser
                  - branch: Google Brain
                  - email: lukaszkaiser@google.com
                - Illia Polosukhin
                  - email: illia.polosukhin@gmail.com

            .function {author}
                name branch email:
                .container
                    **.name**  
                    .branch  
                    .text {.email} size:{small}  
                    .whitespace

            .grid columns:{2} alignment:{spacearound}
                .foreach {.docauthors}
                    name info:
                    .author {.name} {.info::get {branch} orelse:{-}} {.info::get {email}}
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"grid-template-columns: auto auto; justify-content: space-around; align-items: center;\" " +
                    "class=\"stack stack-grid\">" +
                    "<div class=\"container\">" +
                    "<p><strong>Llion Jones</strong><br />Google Research<br />" +
                    "<span style=\"font-size: var(--qd-size-small, 1em);\">" +
                    "<a href=\"llion@google.com\">llion@google.com</a></span>" +
                    "<br /><span>&nbsp;</span></p>" +
                    "</div><div class=\"container\">" +
                    "<p><strong>Aidan N. Gomez</strong><br />University of Toronto<br />" +
                    "<span style=\"font-size: var(--qd-size-small, 1em);\">" +
                    "<a href=\"aidan@cs.toronto.edu\">aidan@cs.toronto.edu</a></span>" +
                    "<br /><span>&nbsp;</span></p>" +
                    "</div><div class=\"container\">" +
                    "<p><strong>Łukasz Kaiser</strong><br />Google Brain<br />" +
                    "<span style=\"font-size: var(--qd-size-small, 1em);\">" +
                    "<a href=\"lukaszkaiser@google.com\">lukaszkaiser@google.com</a></span>" +
                    "<br /><span>&nbsp;</span></p>" +
                    "</div><div class=\"container\">" +
                    "<p><strong>Illia Polosukhin</strong><br />-<br />" +
                    "<span style=\"font-size: var(--qd-size-small, 1em);\">" +
                    "<a href=\"illia.polosukhin@gmail.com\">illia.polosukhin@gmail.com</a></span>" +
                    "<br /><span>&nbsp;</span></p>" +
                    "</div></div>",
                it,
            )
        }
    }
}
