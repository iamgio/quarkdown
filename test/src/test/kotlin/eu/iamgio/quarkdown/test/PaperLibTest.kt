package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Tests for the `paper` library.
 */
class PaperLibTest {
    @Test
    fun paper() {
        execute(
            """
            .doclang {english}
            .include {paper}
            
            .definition
                This is my definition.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertEquals("<p><strong>Definition.</strong> This is my definition.</p>", it)
        }

        execute(
            """
            .doclang {italian}
            .include {paper}
            .numbering
                - definitions: A
            
            .definition
                This is my definition.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertEquals("<p><strong>Definizione A.</strong> This is my definition.</p>", it)
        }

        execute(
            """
            .doclang {english}
            .include {paper}
            .numbering
                - theorems: 1.1
                - proofs: 1.i
            
            .theorem
                This is my theorem.
            
            .proof
                And this is my proof.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertEquals(
                "<p><strong>Theorem 0.1.</strong> This is my theorem.</p>" +
                    "<p><strong>Proof 0.i.</strong> And this is my proof.</p>" +
                    "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\">" +
                    "<span class=\"size-huge\">∎</span>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .doclang {english}
            .include {paper}
            .numbering
                - proofs: a
            
            .paperblocksuffix {:}
            .proofend {#}
            
            .proof
                This is my proof.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertEquals(
                "<p><strong>Proof a:</strong> This is my proof.</p>" +
                    "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\">" +
                    "<span class=\"size-huge\">#</span>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .doclang {english}
            .include {paper}
            
            .abstract
                This is my abstract.
                
            .abstractalignment {end}
            
            .abstract
                This is my second abstract.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertContains(
                it,
                "<div class=\"container fullwidth\" style=\"justify-items: center; text-align: center;\">" +
                    "<p><strong>Abstract</strong></p>" +
                    "</div>",
            )
            assertContains(
                it,
                "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\">" +
                    "<p><strong>Abstract</strong></p>" +
                    "</div>",
            )
        }
    }
}
