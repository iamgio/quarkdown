package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

/**
 * Tests for localization features.
 */
class LocalizationTest {
    @Test
    fun `new localization table`() {
        execute(
            """
            .doclang {english}
            .localization {mytable}
                - English
                    - morning: Good morning
                    - evening: Good evening
                - Italian
                    - morning: Buongiorno
                    - evening: Buonasera
            
            > .localize {mytable:morning}.
            """.trimIndent(),
        ) {
            assertEquals("<blockquote><p>Good morning.</p></blockquote>", it)
        }
    }

    @Test
    fun `localization from function`() {
        execute(
            """
            .doclang {italian}
            .localization {mytable}
                - English
                    - theorem: Theorem
                - Italian
                    - theorem: Teorema

            .function {theorem}
                **.localize {mytable:theorem}.**

            .theorem Test
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Teorema.</strong> Test</p>", it)
        }
    }

    @Test
    fun `missing localization entry`() {
        assertFails {
            execute(
                """
                .doclang {english}
                .localization {mytable}
                    - English
                        - morning: Good morning
                        - evening: Good evening
                    - Italian
                        - morning: Buongiorno
                        - evening: Buonasera
                
                > .localize {mytable:afternoon}.
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `stdlib localization`() {
        execute(
            """
            .doclang {english}
            .localize {std:warning}
            """.trimIndent(),
        ) {
            assertEquals("<p>Warning</p>", it)
        }
    }

    @Test
    fun `duplicate table error`() {
        assertFails {
            execute(
                """
                .localization {mytable}
                    - English
                        - morning: Good morning
                    - Italian
                        - morning: Buongiorno

                .localization {mytable}
                    - English
                        - evening: Good evening
                    - Italian
                        - evening: Buonasera
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun `localization table merge`() {
        execute(
            """
            .doclang {english}
            
            .localization {mytable}
                - English
                    - morning: Good morning
                - Italian
                    - morning: Buongiorno

            .localization {mytable} merge:{yes}
                - English
                    - evening: Good evening
                - Italian
                    - evening: Buonasera
                    
            .localize {mytable:morning}, .localize {mytable:evening}.
            """.trimIndent(),
        ) {
            assertEquals("<p>Good morning, Good evening.</p>", it)
        }
    }

    @Test
    fun `stdlib localization table merge`() {
        execute(
            """
            .doclang {fr-CA}
                    
            .box type:{warning}
                Test
            """.trimIndent(),
        ) {
            assertFalse("<h4>Avertissement</h4>" in it)
        }

        execute(
            """
            .doclang {fr-CA}

            .localization {std} merge:{yes}
                - fr-CA
                    - warning: Avertissement
                    
            .box type:{warning}
                Test
            """.trimIndent(),
        ) {
            assertContains(it, "<h4>Avertissement</h4>")
        }
    }
}
