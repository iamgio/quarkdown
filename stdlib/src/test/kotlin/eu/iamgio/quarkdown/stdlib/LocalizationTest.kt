package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.localization.LocaleLoader
import kotlin.test.BeforeTest

/**
 * [Localization] module tests.
 */
class LocalizationTest {
    private val context = MutableContext(QuarkdownFlavor)

    @BeforeTest
    fun setup() {
        context.documentInfo.locale = LocaleLoader.SYSTEM.fromName("English")!!
    }
/*
    @Test
    fun `localization table`() {
        localization(
            context,
            "mytable",
            MarkdownContent(
                buildBlocks {
                    unorderedList(loose = false) {
                        listItem {
                            paragraph { text("English") }
                            unorderedList(loose = false) {
                                listItem {
                                    paragraph { text("morning: Good morning") }
                                }
                                listItem {
                                    paragraph { text("evening: Good evening") }
                                }
                            }
                        }
                        listItem {
                            paragraph { text("Italian") }
                            unorderedList(loose = false) {
                                listItem {
                                    paragraph { text("morning: Buongiorno") }
                                }
                                listItem {
                                    paragraph { text("evening: Buonasera") }
                                }
                            }
                        }
                    }
                },
            ),
        )

        assertEquals(1, context.localizationTables.size)
        val table = context.localizationTables["mytable"]!!

        assertEquals("Good morning", table[LocaleLoader.SYSTEM.fromName("English")!!]!!["morning"])
        assertEquals("Buongiorno", table[LocaleLoader.SYSTEM.fromName("Italian")!!]!!["morning"])

        assertEquals("Good evening", context.localize("mytable", "evening"))
    }*/
}
