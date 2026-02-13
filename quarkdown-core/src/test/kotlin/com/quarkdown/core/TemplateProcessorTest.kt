package com.quarkdown.core

import com.quarkdown.core.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TemplateProcessor].
 */
class TemplateProcessorTest {
    @Test
    fun `no values`() {
        val template = TemplateProcessor("Hello, world!")
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun empty() {
        val template = TemplateProcessor("")
        assertEquals("", template.process())
    }

    @Test
    fun `single value`() {
        val template =
            TemplateProcessor(
                """
                @param name: String
                Hello, ${'$'}{name}!
                """.trimIndent(),
            )
        template.value("name", "world")
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `double value`() {
        val template =
            TemplateProcessor(
                """
                @param name: String
                @param from: String
                Hello, ${'$'}{name} from ${'$'}{from}!
                """.trimIndent(),
            )
        template.value("name", "world")
        template.value("from", "Quarkdown")
        assertEquals("Hello, world from Quarkdown!", template.process())
    }

    @Test
    fun `single value with default`() {
        val template =
            TemplateProcessor(
                """
                @param name: String = "unnamed"
                Hello, ${'$'}{name}!
                """.trimIndent(),
            )
        assertEquals("Hello, unnamed!", template.process())
    }

    @Test
    fun `single condition`() {
        val trueTemplate =
            TemplateProcessor(
                """
                @param hasName: Boolean = false
                Hello@if(hasName), world@endif!
                """.trimIndent(),
            )

        trueTemplate.conditional("hasName", false)
        assertEquals("Hello!", trueTemplate.process())

        trueTemplate.conditional("hasName", true)
        assertEquals("Hello, world!", trueTemplate.process())
    }

    @Test
    fun `double condition`() {
        val template =
            TemplateProcessor(
                """
                @param hasName: Boolean = false
                @param ask: Boolean = false
                Hello@if(hasName), world@endif!@if(ask) How are you?@endif
                """.trimIndent(),
            )

        template.conditional("hasName", false)
        template.conditional("ask", false)
        assertEquals("Hello!", template.process())

        template.conditional("ask", true)
        assertEquals("Hello! How are you?", template.process())

        template.conditional("hasName", true)
        assertEquals("Hello, world! How are you?", template.process())

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `single value and single condition`() {
        val template =
            TemplateProcessor(
                """
                @param name: String
                @param ask: Boolean = false
                Hello, ${'$'}{name}!@if(ask) How are you?@endif
                """.trimIndent(),
            )
        template.value("name", "world")

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.process())

        template.conditional("ask", true)
        assertEquals("Hello, world! How are you?", template.process())
    }

    @Test
    fun `optional value as condition`() {
        val template =
            TemplateProcessor(
                """
                @param name: String? = null
                Hello@if(name != null), ${'$'}{name}@endif!
                """.trimIndent(),
            )

        template.optionalValue("name", "world")
        assertEquals("Hello, world!", template.process())

        template.optionalValue("name", null)
        assertEquals("Hello!", template.process())
    }

    @Test
    fun `optional value if-else`() {
        val template =
            TemplateProcessor(
                """
                @param name: String? = null
                Hello, @if(name != null)${'$'}{name}@endif@if(name == null)unnamed@endif!
                """.trimIndent(),
            )

        template.optionalValue("name", "world")
        assertEquals("Hello, world!", template.process())

        template.optionalValue("name", null)
        assertEquals("Hello, unnamed!", template.process())
    }

    @Test
    fun multiline() {
        val template =
            TemplateProcessor(
                """
                @param name: String
                @param ask: Boolean = false
                Hello, ${'$'}{name}!
                @if(ask)
                How are you?
                I hope you are good.
                @endif
                """.trimIndent(),
            )
        template.value("name", "world")

        template.conditional("ask", true)
        assertEquals(
            """
            Hello, world!
            How are you?
            I hope you are good.
            """.trimIndent(),
            template.process(),
        )

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.process())
    }

    @Test
    fun `multiline with delimiter`() {
        val template =
            TemplateProcessor(
                """
                @param a: Boolean = false
                @param b: Boolean = false
                @if(a)
                A
                @endif
                X
                @if(b)
                B
                @endif
                """.trimIndent(),
            )
        template.conditional("a", true)
        template.conditional("b", false)
        assertEquals("A\nX", template.process())

        template.conditional("a", false)
        template.conditional("b", false)
        assertEquals("X", template.process())

        template.conditional("a", false)
        template.conditional("b", true)
        assertEquals("X\nB", template.process())
    }

    @Test
    fun `multiline with spaced delimiter`() {
        // Blank lines act as separators between content sections.
        // They are placed inside the conditional blocks to control their visibility.
        val template =
            TemplateProcessor(
                """
                @param a: Boolean = false
                @param b: Boolean = false
                @if(a)
                A
                @endif

                X

                @if(b)
                B
                @endif
                """.trimIndent(),
            )
        template.conditional("a", true)
        template.conditional("b", false)
        assertEquals("A\nX", template.process())

        template.conditional("a", false)
        template.conditional("b", false)
        assertEquals("X", template.process())

        template.conditional("a", false)
        template.conditional("b", true)
        assertEquals("X\n\nB", template.process())
    }

    @Test
    fun `multiline with gap`() {
        val template =
            TemplateProcessor(
                """
                @param a: Boolean = false
                @param b: Boolean = false
                @param c: Boolean = false
                @if(a)
                A
                @endif
                @if(b)
                B
                @endif
                @if(c)
                C
                @endif
                """.trimIndent(),
            )
        template.conditional("a", true)
        template.conditional("b", false)
        template.conditional("c", true)
        assertEquals("A\nC", template.process())
    }

    @Test
    fun `from resource`() {
        val template = TemplateProcessor.fromResourceName("/template/template.kte")
        template.optionalValue("name", "world")
        template.conditional("ask", true)

        assertEquals(
            """
            Hello, world!

            How are you?
            I'm good.
            """.trimIndent(),
            template.process(),
        )
    }

    @Test
    fun `for each with no delimiters`() {
        val template =
            TemplateProcessor(
                """
                @param names: List<String> = emptyList()
                Hello, @for(name in names)${'$'}{name}@endfor!
                """.trimIndent(),
            )
        template.iterable("names", listOf("Alice", "Bob", "Charlie"))
        assertEquals("Hello, AliceBobCharlie!", template.process())
    }

    @Test
    fun `for each with delimiters`() {
        val template =
            TemplateProcessor(
                """
                @param names: List<String> = emptyList()
                Hello, @for(name in names)${'$'}{name},@endfor!
                """.trimIndent(),
            )
        template.iterable("names", listOf("Alice", "Bob", "Charlie"))
        assertEquals("Hello, Alice,Bob,Charlie,!", template.process())
    }

    @Test
    fun `multiline for each`() {
        val template =
            TemplateProcessor(
                """
                @param items: List<String> = emptyList()
                Groceries:
                @for(item in items)
                - ${'$'}{item}
                @endfor
                """.trimIndent(),
            )
        template.iterable("items", listOf("Apples", "Bananas", "Carrots"))
        assertEquals(
            """
            Groceries:
            - Apples
            - Bananas
            - Carrots
            """.trimIndent(),
            template.process(),
        )
    }

    @Test
    fun `empty for each`() {
        val template =
            TemplateProcessor(
                """
                @param items: List<String> = emptyList()
                Groceries:
                @for(item in items)
                - ${'$'}{item}
                @endfor
                """.trimIndent(),
            )
        template.iterable("items", emptyList())
        assertEquals("Groceries:", template.process())
    }

    @Test
    fun `nested for each`() {
        val template =
            TemplateProcessor(
                """
                @param items: List<String> = emptyList()
                @param letters: List<String> = emptyList()
                Groceries:
                @for(item in items)
                @for(letter in letters)
                - ${'$'}{item} ${'$'}{letter}
                @endfor
                @endfor
                """.trimIndent(),
            )
        template.iterable("items", listOf("Apple", "Banana"))
        template.iterable("letters", listOf("A", "B"))
        assertEquals(
            """
            Groceries:
            - Apple A
            - Apple B
            - Banana A
            - Banana B
            """.trimIndent(),
            template.process(),
        )
    }
}
