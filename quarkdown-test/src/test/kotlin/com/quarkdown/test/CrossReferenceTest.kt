package com.quarkdown.test

import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for cross-references.
 */
class CrossReferenceTest {
    @Test
    fun `invalid reference`() {
        execute("See .ref {x}") {
            assertEquals("<p>See [???]</p>", it)
        }
    }

    @Test
    fun `reference after definition (heading)`() {
        execute(
            """
            ## Title {#my-ref}
            
            See .ref {my-ref}.
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2 id=\"my-ref\">Title</h2>" +
                    "<p>See <a href=\"#my-ref\"><span class=\"cross-reference\">Title</span></a>.</p>",
                it,
            )
        }
    }

    @Test
    fun `reference before definition (heading)`() {
        execute(
            """
            See .ref {my-ref}.
            
            ## Title {#my-ref}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <a href=\"#my-ref\"><span class=\"cross-reference\">Title</span></a>.</p>" +
                    "<h2 id=\"my-ref\">Title</h2>",
                it,
            )
        }
    }

    @Test
    fun `multiple references to the same definition (heading)`() {
        execute(
            """
            See .ref {my-ref} and .ref {my-ref}.
            
            ## Title {#my-ref}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <a href=\"#my-ref\"><span class=\"cross-reference\">Title</span></a> and " +
                    "<a href=\"#my-ref\"><span class=\"cross-reference\">Title</span></a>.</p>" +
                    "<h2 id=\"my-ref\">Title</h2>",
                it,
            )
        }
    }

    @Test
    fun `mutual references (heading)`() {
        execute(
            """
            See .ref {ref-a}.
            
            ## Title A {#ref-a}
            
            See also .ref {ref-b}.
            
            ## Title B {#ref-b}
            
            Back to .ref {ref-a}.
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <a href=\"#ref-a\"><span class=\"cross-reference\">Title A</span></a>.</p>" +
                    "<h2 id=\"ref-a\">Title A</h2>" +
                    "<p>See also <a href=\"#ref-b\"><span class=\"cross-reference\">Title B</span></a>.</p>" +
                    "<h2 id=\"ref-b\">Title B</h2>" +
                    "<p>Back to <a href=\"#ref-a\"><span class=\"cross-reference\">Title A</span></a>.</p>",
                it,
            )
        }
    }

    @Test
    fun `numbered references (heading)`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1.1
            
            See .ref {first-ref} and .ref {second-ref}.
            
            # Title {#first-ref}
            
            ## Subitle {#second-ref}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <a href=\"#first-ref\"><span class=\"cross-reference\" data-location=\"1\"></span></a>" +
                    " and <a href=\"#second-ref\"><span class=\"cross-reference\" data-location=\"1.1\"></span></a>.</p>" +
                    "<h1 id=\"first-ref\" data-location=\"1\">Title</h1>" +
                    "<h2 id=\"second-ref\" data-location=\"1.1\">Subitle</h2>",
                it,
            )
        }
    }

    @Test
    fun `localized numbered references (heading)`() {
        execute(
            """
            .noautopagebreak
            .doclang {en}
            .numbering
                - headings: 1.1
            
            See .ref {first-ref} and .ref {second-ref}.
            
            # Title {#first-ref}
            
            ## Subitle {#second-ref}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <a href=\"#first-ref\">" +
                    "<span class=\"cross-reference\" data-location=\"1\" data-localized-kind=\"Section\"></span></a>" +
                    " and <a href=\"#second-ref\">" +
                    "<span class=\"cross-reference\" data-location=\"1.1\" data-localized-kind=\"Section\"></span></a>.</p>" +
                    "<h1 id=\"first-ref\" data-location=\"1\">Title</h1>" +
                    "<h2 id=\"second-ref\" data-location=\"1.1\">Subitle</h2>",
                it,
            )
        }
    }

    @Test
    fun `reference after definition (figure, no caption)`() {
        execute(
            """
            ![My Image](img.png) {#my-fig}
            
            See .ref {my-fig}.
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><img src=\"img.png\" alt=\"My Image\" /></figure>" +
                    "<p>See <span class=\"cross-reference\">my-fig</span>.</p>",
                it,
            )
        }
    }

    @Test
    fun `reference after definition (figure, with caption)`() {
        execute(
            """
            ![My Image](img.png "The caption") {#my-fig}
            
            See .ref {my-fig}.
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><img src=\"img.png\" alt=\"My Image\" title=\"The caption\" />" +
                    "<figcaption class=\"caption-bottom\">The caption</figcaption></figure>" +
                    "<p>See <span class=\"cross-reference\">The caption</span>.</p>",
                it,
            )
        }
    }

    @Test
    fun `multiple references to the same definition (figure)`() {
        execute(
            """
            See .ref {my-fig} and .ref {my-fig}.
            
            ![My Image](img.png) {#my-fig}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\">my-fig</span> and " +
                    "<span class=\"cross-reference\">my-fig</span>.</p>" +
                    "<figure><img src=\"img.png\" alt=\"My Image\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `numbered references (figure)`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - figures: a
            
            See .ref {my-fig}.
            
            ![My Image](img.png "The caption") {#my-fig}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"a\"></span>.</p>" +
                    "<figure id=\"figure-a\">" +
                    "<img src=\"img.png\" alt=\"My Image\" title=\"The caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-location=\"a\">The caption</figcaption>" +
                    "</figure>",
                it,
            )
        }
    }

    @Test
    fun `localized numbered references (figure)`() {
        execute(
            """
            .noautopagebreak
            .doclang {en}
            .numbering
                - figures: a
            
            See .ref {my-fig}.
            
            ![My Image](img.png "The caption") {#my-fig}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"a\" data-localized-kind=\"Figure\"></span>.</p>" +
                    "<figure id=\"figure-a\">" +
                    "<img src=\"img.png\" alt=\"My Image\" title=\"The caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-location=\"a\" data-localized-kind=\"Figure\">The caption</figcaption>" +
                    "</figure>",
                it,
            )
        }
    }
}
