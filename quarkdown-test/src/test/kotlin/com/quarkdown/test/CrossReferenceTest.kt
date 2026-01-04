package com.quarkdown.test

import com.quarkdown.rendering.plaintext.extension.plainText
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
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
    fun `localized numbered references (heading, plaintext)`() {
        execute(
            """
            .doclang {en}
            .numbering
                - headings: 1.1
            
            See .ref {first-ref} and .ref {second-ref}.
            
            # Title {#first-ref}
            
            ## Subtitle {#second-ref}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
            renderer = { factory, ctx -> factory.plainText(ctx) },
        ) {
            assertEquals(
                "See Section 1 and Section 1.1.\n\n" +
                    "Title\n\n" +
                    "Subtitle\n\n",
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

    @Test
    fun `numbered references (mermaid figure)`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - figures: A
            
            See .ref {my-diagram} and .ref {my-other-diagram}.
            
            .mermaid ref:{my-diagram}
                graph TD
                    A --> B
                    
            .mermaid caption:{My other diagram} ref:{my-other-diagram}
                graph TD
                    A --> B
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"A\"></span>" +
                    " and <span class=\"cross-reference\" data-location=\"B\"></span>.</p>" +
                    "<figure id=\"figure-A\">" +
                    "<pre class=\"mermaid fill-height\">graph TD\n    A --&gt; B</pre>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"A\"></figcaption>" +
                    "</figure>" +
                    "<figure id=\"figure-B\">" +
                    "<pre class=\"mermaid fill-height\">graph TD\n    A --&gt; B</pre>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"B\">My other diagram</figcaption>" +
                    "</figure>",
                it,
            )
        }
    }

    @Test
    fun `numbered references (custom figure)`() {
        execute(
            """
            .numbering
                - figures: 1
            
            See .ref {my-fig} and .ref {my-other-fig}.
            
            .figure ref:{my-fig}
                This is a custom figure.
            
            .figure caption:{My caption} ref:{my-other-fig}
                This is another custom figure.
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"1\"></span> and " +
                    "<span class=\"cross-reference\" data-location=\"2\"></span>.</p>" +
                    "<figure id=\"figure-1\">" +
                    "<p>This is a custom figure.</p>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"1\"></figcaption>" +
                    "</figure>" +
                    "<figure id=\"figure-2\">" +
                    "<p>This is another custom figure.</p>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"2\">My caption</figcaption>" +
                    "</figure>",
                it,
            )
        }
    }

    @Test
    fun `reference before definition (table)`() {
        execute(
            """
            See .ref {my-table}.
            
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            {#my-table}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\">my-table</span>.</p>" +
                    "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody></table>",
                it,
            )
        }
    }

    @Test
    fun `localized numbered references (table)`() {
        execute(
            """
            .doclang {en}
            .numbering
                - tables: i
            
            See .ref {my-table} and .ref {my-other-table}.
            
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            {#my-table}
            
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            "My caption" {#my-other-table}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"i\" data-localized-kind=\"Table\"></span> " +
                    "and <span class=\"cross-reference\" data-location=\"ii\" data-localized-kind=\"Table\"></span>.</p>" +
                    "<table id=\"table-i\"><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-location=\"i\" data-localized-kind=\"Table\"></caption></table>" +
                    "<table id=\"table-ii\"><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-location=\"ii\" data-localized-kind=\"Table\">My caption</caption></table>",
                it,
            )
        }
    }

    @Test
    fun `numbered references (csv table)`() {
        execute(
            """
            .numbering
                - tables: 1
                
            See .ref {my-table} and .ref {my-other-table}.
            
            .csv {csv/people.csv} ref:{my-table}
            
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            {#my-other-table}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertContains(
                it,
                "<p>See <span class=\"cross-reference\" data-location=\"1\"></span> and " +
                    "<span class=\"cross-reference\" data-location=\"2\"></span>.</p>",
            )
            assertContains(
                it,
                "<caption class=\"caption-bottom\" data-location=\"1\"></caption></table>",
            )
            assertContains(
                it,
                "<caption class=\"caption-bottom\" data-location=\"2\"></caption></table>",
            )
        }
    }

    @Test
    fun `numbered references (math)`() {
        execute(
            """
            .numbering
                - equations: 1
                
            See .ref {my-math} and .ref {my-other-math}.
            
            $ E = mc^2 $ {#my-math}
            
            $$$ {#my-other-math}
            E = mc^2
            $$$
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"1\"></span> and " +
                    "<span class=\"cross-reference\" data-location=\"2\"></span>.</p>" +
                    "<formula data-block=\"\" data-location=\"1\">E = mc^2</formula>" +
                    "<formula data-block=\"\" data-location=\"2\">E = mc^2</formula>",
                it,
            )
        }
    }

    @Test
    fun `reference before definition (code block)`() {
        execute(
            """
            See .ref {my-code}.
            
            ```kotlin {#my-code}
            println("Hello, World!")
            ```
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\">my-code</span>.</p>" +
                    "<pre><code class=\"language-kotlin\">println(&quot;Hello, World!&quot;)</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `reference before definition (code block from function)`() {
        execute(
            """
            See .ref {my-code}.
            
            .code lang:{kotlin} ref:{my-code}
                println("Hello, World!")
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\">my-code</span>.</p>" +
                    "<pre><code class=\"language-kotlin\">println(&quot;Hello, World!&quot;)</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `localized numbered references (code block)`() {
        execute(
            """
            .doclang {en}
            .numbering
                - code: I
            
            See .ref {my-code} and .ref {my-other-code}.
            
            ```kotlin {#my-code}
            println("Hello, World!")
            ```
            
            ```kotlin {#my-other-code}
            println(
                "Hello, World!"
            )
            ```
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"I\" data-localized-kind=\"Listing\"></span> and " +
                    "<span class=\"cross-reference\" data-location=\"II\" data-localized-kind=\"Listing\"></span>.</p>" +
                    "<figure id=\"listing-I\"><pre><code class=\"language-kotlin\">" +
                    "println(&quot;Hello, World!&quot;)</code></pre>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"I\" data-localized-kind=\"Listing\"></figcaption></figure>" +
                    "<figure id=\"listing-II\"><pre><code class=\"language-kotlin\">" +
                    "println(\n    &quot;Hello, World!&quot;\n)</code></pre>" +
                    "<figcaption class=\"caption-bottom\" data-location=\"II\" data-localized-kind=\"Listing\"></figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun `custom numbered blocks`() {
        execute(
            """
            .numbering
                - myblock: a
            
            See .ref {block1} and .ref {block2}.
            
            .numbered {myblock} ref:{block1}
                Block .1
            
            .numbered {myblock} ref:{block2}
                Block .1
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<p>See <span class=\"cross-reference\" data-location=\"a\"></span> and " +
                    "<span class=\"cross-reference\" data-location=\"b\"></span>.</p>" +
                    "<p>Block a</p>" +
                    "<p>Block b</p>",
                it,
            )
        }
    }
}
