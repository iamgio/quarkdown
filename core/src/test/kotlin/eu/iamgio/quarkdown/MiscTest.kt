package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.id.getId
import eu.iamgio.quarkdown.ast.quarkdown.block.TableOfContents
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.locale.JVMLocaleLoader
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.Media
import eu.iamgio.quarkdown.media.RemoteMedia
import eu.iamgio.quarkdown.rendering.html.HtmlIdentifierProvider
import eu.iamgio.quarkdown.rendering.html.QuarkdownHtmlNodeRenderer
import eu.iamgio.quarkdown.util.flattenedChildren
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for miscellaneous classes.
 */
class MiscTest {
    @Test
    fun treeVisit() {
        val node =
            AstRoot(
                listOf(
                    BlockQuote(
                        listOf(
                            Paragraph(listOf(Text("abc"))),
                        ),
                    ),
                    Paragraph(
                        listOf(
                            Strong(listOf(Text("abc"))),
                            Text("def"),
                            Emphasis(listOf(Text("ghi"))),
                        ),
                    ),
                    Code("Hello, world!", language = "java"),
                ),
            )

        with(node.flattenedChildren().map { it::class.simpleName }.toList()) {
            assertEquals(
                listOf(
                    "BlockQuote",
                    "Paragraph",
                    "Text",
                    "Paragraph",
                    "Strong",
                    "Text",
                    "Text",
                    "Emphasis",
                    "Text",
                    "Code",
                ),
                this,
            )
        }
    }

    @Test
    fun identifiers() {
        val provider = HtmlIdentifierProvider.of(QuarkdownHtmlNodeRenderer(MutableContext(QuarkdownFlavor)))
        assertEquals("abc", provider.getId(Heading(1, listOf(Text("Abc")))))
        assertEquals("abc-def", provider.getId(Heading(1, listOf(Strong(listOf(Text("Abc Def")))))))
        assertEquals("hello-world", provider.getId(Heading(1, listOf(Text("Hello, World!")))))
    }

    @Test
    fun tableOfContents() {
        val headings1 =
            sequenceOf(
                Heading(1, listOf(Text("ABC"))),
                Heading(2, listOf(Text("DEF"))),
                Heading(2, listOf(Text("GHI"))),
                Heading(3, listOf(Text("JKL"))),
                Heading(2, listOf(Text("MNO"))),
                Heading(1, listOf(Text("PQR"))),
            )

        TableOfContents.generate(headings1, maxDepth = 3).let { toc ->
            assertEquals(2, toc.items.size)
            assertEquals(3, toc.items[0].subItems.size)
            assertEquals(1, toc.items[0].subItems[1].subItems.size)

            assertEquals(Text("ABC"), toc.items[0].text.first())
            assertEquals(Text("DEF"), toc.items[0].subItems[0].text.first())
            assertEquals(Text("GHI"), toc.items[0].subItems[1].text.first())
            assertEquals(Text("JKL"), toc.items[0].subItems[1].subItems[0].text.first())
            assertEquals(Text("MNO"), toc.items[0].subItems[2].text.first())
            assertEquals(Text("PQR"), toc.items[1].text.first())
        }

        TableOfContents.generate(headings1, maxDepth = 2).let { toc ->
            assertEquals(2, toc.items.size)

            assertEquals(Text("ABC"), toc.items[0].text.first())
            assertEquals(Text("DEF"), toc.items[0].subItems[0].text.first())
            assertEquals(Text("GHI"), toc.items[0].subItems[1].text.first())
            assertTrue(toc.items[0].subItems[1].subItems.isEmpty())
            assertEquals(Text("MNO"), toc.items[0].subItems[2].text.first())
            assertEquals(Text("PQR"), toc.items[1].text.first())
        }

        val headings2 =
            sequenceOf(
                Heading(1, listOf(Text("ABC"))),
                Heading(3, listOf(Text("DEF"))),
                Heading(2, listOf(Text("GHI"))),
            )

        TableOfContents.generate(headings2, maxDepth = 3).let { toc ->
            assertEquals(1, toc.items.size)
            assertEquals(2, toc.items[0].subItems.size)

            assertEquals(Text("ABC"), toc.items[0].text.first())
            assertEquals(Text("DEF"), toc.items[0].subItems[0].text.first())
            assertEquals(Text("GHI"), toc.items[0].subItems[1].text.first())
        }

        /*
        val headings3 =
            sequenceOf(
                Heading(2, listOf(Text("ABC"))),
                Heading(3, listOf(Text("DEF"))),
                Heading(2, listOf(Text("GHI"))),
                Heading(1, listOf(Text("JKL"))),
            )

        TableOfContents.generate(headings3, maxDepth = 3).let { toc ->
            println(toc.items[0].subItems)
            assertEquals(3, toc.items.size)
            assertEquals(1, toc.items[0].subItems.size)

            assertEquals(Text("ABC"), toc.items[0].text.first())
            assertEquals(Text("DEF"), toc.items[0].subItems[0].text.first())
            assertEquals(Text("GHI"), toc.items[1].text.first())
            assertEquals(Text("JKL"), toc.items[1].text.first())
        }
         */
    }

    @Test
    fun locale() {
        val retriever = JVMLocaleLoader

        with(retriever.fromTag("en")) {
            assertNotNull(this)
            assertEquals(this, retriever.fromName("English"))
            assertEquals(this, retriever.find("English"))
            assertEquals(this, retriever.find("eNgLiSh"))
            assertEquals("en", code)
            assertEquals("en", tag)
            assertEquals("English", localizedName)
            assertNull(countryCode)
            assertNull(localizedCountryName)
        }

        with(retriever.find("it")) {
            assertNotNull(this)
            assertEquals(this, retriever.fromName("Italian"))
            assertEquals(this, retriever.find("Italian"))
            assertEquals(this, retriever.find("iTaLiAn"))
            assertEquals("it", code)
            assertEquals("it", tag)
            assertEquals("italiano", localizedName)
            assertNull(countryCode)
            assertNull(localizedCountryName)
        }

        with(retriever.find("en-US")) {
            assertNotNull(this)
            assertEquals(this, retriever.find("English (United States)"))
            assertEquals(this, retriever.find("En-us"))
            assertEquals("en", code)
            assertEquals("en-US", tag)
            assertEquals("English (United States)", localizedName)
            assertEquals("US", countryCode)
            assertEquals("United States", localizedCountryName)
        }

        with(retriever.find("fr-CA")) {
            assertNotNull(this)
            assertEquals(this, retriever.find("French (Canada)"))
            assertEquals("fr", code)
            assertEquals("fr-CA", tag)
            assertEquals("français (Canada)", localizedName)
            assertEquals("CA", countryCode)
            assertEquals("Canada", localizedCountryName)
        }

        assertNull(retriever.fromTag("nonexistent"))
        assertNull(retriever.fromName("nonexistent"))
        assertNull(retriever.find("nonexistent"))

        assertTrue(retriever.all.iterator().hasNext())
    }

    @Test
    fun media() {
        assertIs<LocalMedia>(Media.of("src/main/resources/render/html-wrapper.html"))
        assertIs<RemoteMedia>(Media.of("https://example.com/image.jpg"))
        assertFails { Media.of("nonexistent") }
        assertFails { Media.of("src") } // Directory
    }
}
