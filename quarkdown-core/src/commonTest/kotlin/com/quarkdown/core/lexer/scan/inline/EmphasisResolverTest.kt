package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.scan.inline.base.CriticalContentRecognizer
import com.quarkdown.core.lexer.scan.inline.base.DelimiterRunRecognizer
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.PlainTextToken
import com.quarkdown.core.lexer.tokens.StrikethroughToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken
import kotlin.test.Test
import kotlin.test.assertEquals

class EmphasisResolverTest {
    /**
     * @return the nesting the lexer produced, as `em[...]`, `strong[...]`, `se[...]`, `del[...]` and `t`
     */
    private fun shape(source: String): String =
        InlineScanLexer(source, listOf(DelimiterRunRecognizer, CriticalContentRecognizer), ::PlainTextToken)
            .tokenize()
            .joinToString("") { it.shape() }

    private fun Token.shape(): String =
        when (this) {
            is EmphasisToken -> "em[${children.joinToString("") { it.shape() }}]"
            is StrongToken -> "strong[${children.joinToString("") { it.shape() }}]"
            is StrongEmphasisToken -> "se[${children.joinToString("") { it.shape() }}]"
            is StrikethroughToken -> "del[${children.joinToString("") { it.shape() }}]"
            else -> "t"
        }

    @Test
    fun `pairs the simple forms`() {
        assertEquals("em[t]", shape("*foo*"))
        assertEquals("em[t]", shape("_foo_"))
        assertEquals("strong[t]", shape("**foo**"))
        assertEquals("se[t]", shape("***foo***"))
        assertEquals("se[t]", shape("___foo___"))
        assertEquals("del[t]", shape("~~foo~~"))
    }

    @Test
    fun `nests per CommonMark rather than per the old approximation`() {
        assertEquals("em[strong[t]t]", shape("***foo** bar*"))
        assertEquals("em[tstrong[t]t]", shape("*foo **bar** baz*"))
        // The closing single asterisk takes one delimiter from the `**` opener, and the closing `**` then
        // pairs its remainder with what is left of both openers, exactly as CommonMark 0.31.2 example 414
        // splits a run across two pairs. CommonMarkEmphasisTest is the authority for this algorithm.
        assertEquals("em[tem[em[t]t]]", shape("*foo **bar* baz**"))
        assertEquals("em[strong[t]]", shape("*__foo__*"))
    }

    @Test
    fun `leaves unmatched delimiters as one plain text token`() {
        assertEquals("t", shape("*foo"))
        assertEquals("t", shape("a_b_c"))
        assertEquals("t", shape("**"))
        assertEquals("t", shape("~foo~"))
        assertEquals("t", shape("~~~foo~~~"))
    }

    @Test
    fun `applies the rule of three`() {
        assertEquals("tem[t]", shape("foo*bar*"))
        assertEquals("tse[t]t", shape("foo***bar***baz"))
    }
}
