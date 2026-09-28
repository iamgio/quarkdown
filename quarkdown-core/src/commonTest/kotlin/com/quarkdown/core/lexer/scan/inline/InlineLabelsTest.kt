package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.scan.Scanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InlineLabelsTest {
    private fun label(source: String) = Scanner(source).takeLabel { takeLinkTarget() }

    @Test
    fun `ends the label at the earliest bracket the rest accepts`() {
        val (text, target) = label("[a [b] c](u)")!!
        assertEquals("a [b] c", text)
        assertEquals("u", target.url)
        assertEquals("", label("[](u)")!!.first)
    }

    @Test
    fun `parses titles and parentheses balanced at any depth`() {
        assertEquals("\"t\"", label("[l](u \"t\")")!!.second.title)
        assertEquals("<u v>", label("[l](<u v>)")!!.second.url)
        assertEquals("u(1)x", label("[l](u(1)x)")!!.second.url)
        assertEquals("a(b(c(d)))", label("[l](a(b(c(d))))")!!.second.url)
        assertEquals("", label("[l]()")!!.second.url)
        assertNull(label("[l](u"))
        assertNull(label("[l"))
    }
}
