package com.quarkdown.grammargen

import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.grammargen.textmate.TextMateFormat
import com.quarkdown.grammargen.textmate.TextMatePattern
import com.quarkdown.grammargen.textmate.capturing
import com.quarkdown.grammargen.textmate.textMate
import org.junit.Test
import kotlin.test.assertContains

/**
 * Tests for the TextMate grammar generation.
 */
class TextMateGrammarGenTest {
    // A mock clone of the TextMateFormat with a simple regex pattern for testing purposes.
    private val format =
        object : GrammarFormat<TextMatePattern> by TextMateFormat() {
            override fun createPatterns(): List<TextMatePattern> {
                val regexPattern =
                    TokenRegexPattern(
                        name = "mock",
                        wrap = { throw UnsupportedOperationException() },
                        regex = "\\w+".toRegex(),
                    )
                return listOf(
                    regexPattern textMate "mock1",
                    regexPattern textMate "mock2" capturing (1 to "x") capturing (2 to "y"),
                )
            }
        }

    @Test
    fun generation() {
        val output = GrammarGen.generate(format)
        assertContains(output, "\"name\": \"Quarkdown\"")
        assertContains(output, """{ "name": "mock1", "match": "\\w+" }""")
        assertContains(output, """{ "name": "mock2", "match": "\\w+", "captures": { "1": { "name": "x" }, "2": { "name": "y" } } }""")
    }
}
