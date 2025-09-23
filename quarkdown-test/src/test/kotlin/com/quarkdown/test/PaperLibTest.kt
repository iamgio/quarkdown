package com.quarkdown.test

import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Tests for the `paper` library.
 */
class PaperLibTest {
    @Test
    fun definition() {
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
    }

    @Test
    fun `definition numbering`() {
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
            options = DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals("<p><strong>Definizione A.</strong> This is my definition.</p>", it)
        }
    }

    @Test
    fun `theorem numbering`() {
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
            options = DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
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
    }

    @Test
    fun `proof customization`() {
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
            options = DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p><strong>Proof a:</strong> This is my proof.</p>" +
                    "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\">" +
                    "<span class=\"size-huge\">#</span>" +
                    "</div>",
                it,
            )
        }
    }

    @Test
    fun `custom named paragraph`() {
        execute(
            """
            .doclang {english}
            .include {paper}
            
            .namedparagraph {Problem}
                This is my problem.
            
            .namedparagraph {Solution}
                This is my solution.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
        ) {
            assertEquals(
                "<p><strong>Problem.</strong> This is my problem.</p>" +
                    "<p><strong>Solution.</strong> This is my solution.</p>",
                it,
            )
        }
    }

    @Test
    fun `numbered custom named paragraph`() {
        execute(
            """
            .doclang {english}
            .include {paper}
            
            .numbering
                - problem: a
                
            .function {problem}
                content:
                .namedparagraph {Problem} tag:{problem} content:{.content}
            
            .problem
                This is my problem.
                
            .problem
                This is another problem.
            """.trimIndent(),
            loadableLibraries = setOf("paper"),
            options = DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p><strong>Problem a.</strong> This is my problem.</p>" +
                    "<p><strong>Problem b.</strong> This is another problem.</p>",
                it,
            )
        }
    }

    @Test
    fun `abstract`() {
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
                    "<h4>Abstract</h4>" +
                    "</div>",
            )
            assertContains(
                it,
                "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\">" +
                    "<h4>Abstract</h4>" +
                    "</div>",
            )
        }
    }
}
