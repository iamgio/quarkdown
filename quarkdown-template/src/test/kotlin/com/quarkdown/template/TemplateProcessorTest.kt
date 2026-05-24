package com.quarkdown.template

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TemplateProcessor].
 *
 * Templates live as precompiled JTE fixtures under `src/test/jte/test/`, mirroring the
 * production model where the runtime engine never compiles templates on the fly.
 */
class TemplateProcessorTest {
    /**
     * Renders the template and normalizes line endings to LF.
     */
    private fun TemplateProcessor.render(): String = process().toString().replace("\r\n", "\n")

    @Test
    fun `no values`() {
        val template = TemplateProcessor("test/no_values.jte")
        assertEquals("Hello, world!", template.render())
    }

    @Test
    fun empty() {
        val template = TemplateProcessor("test/empty.jte")
        assertEquals("", template.render())
    }

    @Test
    fun `single value`() {
        val template = TemplateProcessor("test/single_value.jte")
        template.value("name", "world")
        assertEquals("Hello, world!", template.render())
    }

    @Test
    fun `double value`() {
        val template = TemplateProcessor("test/double_value.jte")
        template.value("name", "world")
        template.value("from", "Quarkdown")
        assertEquals("Hello, world from Quarkdown!", template.render())
    }

    @Test
    fun `single value with default`() {
        val template = TemplateProcessor("test/single_value_with_default.jte")
        assertEquals("Hello, unnamed!", template.render())
    }

    @Test
    fun `single condition`() {
        val template = TemplateProcessor("test/single_condition.jte")

        template.conditional("hasName", false)
        assertEquals("Hello!", template.render())

        template.conditional("hasName", true)
        assertEquals("Hello, world!", template.render())
    }

    @Test
    fun `double condition`() {
        val template = TemplateProcessor("test/double_condition.jte")

        template.conditional("hasName", false)
        template.conditional("ask", false)
        assertEquals("Hello!", template.render())

        template.conditional("ask", true)
        assertEquals("Hello! How are you?", template.render())

        template.conditional("hasName", true)
        assertEquals("Hello, world! How are you?", template.render())

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.render())
    }

    @Test
    fun `single value and single condition`() {
        val template = TemplateProcessor("test/single_value_and_single_condition.jte")
        template.value("name", "world")

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.render())

        template.conditional("ask", true)
        assertEquals("Hello, world! How are you?", template.render())
    }

    @Test
    fun `optional value as condition`() {
        val template = TemplateProcessor("test/optional_value_as_condition.jte")

        template.optionalValue("name", "world")
        assertEquals("Hello, world!", template.render())

        template.optionalValue("name", null)
        assertEquals("Hello!", template.render())
    }

    @Test
    fun `optional value if-else`() {
        val template = TemplateProcessor("test/optional_value_if_else.jte")

        template.optionalValue("name", "world")
        assertEquals("Hello, world!", template.render())

        template.optionalValue("name", null)
        assertEquals("Hello, unnamed!", template.render())
    }

    @Test
    fun multiline() {
        val template = TemplateProcessor("test/multiline.jte")
        template.value("name", "world")

        template.conditional("ask", true)
        assertEquals(
            """
            Hello, world!
            How are you?
            I hope you are good.
            """.trimIndent(),
            template.render(),
        )

        template.conditional("ask", false)
        assertEquals("Hello, world!", template.render())
    }

    @Test
    fun `multiline with delimiter`() {
        val template = TemplateProcessor("test/multiline_with_delimiter.jte")

        template.conditional("a", true)
        template.conditional("b", false)
        assertEquals("A\nX", template.render())

        template.conditional("a", false)
        template.conditional("b", false)
        assertEquals("X", template.render())

        template.conditional("a", false)
        template.conditional("b", true)
        assertEquals("X\nB", template.render())
    }

    @Test
    fun `multiline with spaced delimiter`() {
        // Blank lines act as separators between content sections.
        // They are placed inside the conditional blocks to control their visibility.
        val template = TemplateProcessor("test/multiline_with_spaced_delimiter.jte")

        template.conditional("a", true)
        template.conditional("b", false)
        assertEquals("A\nX", template.render())

        template.conditional("a", false)
        template.conditional("b", false)
        assertEquals("X", template.render())

        template.conditional("a", false)
        template.conditional("b", true)
        assertEquals("X\n\nB", template.render())
    }

    @Test
    fun `multiline with gap`() {
        val template = TemplateProcessor("test/multiline_with_gap.jte")
        template.conditional("a", true)
        template.conditional("b", false)
        template.conditional("c", true)
        assertEquals("A\nC", template.render())
    }

    @Test
    fun `from resource`() {
        val template = TemplateProcessor("template/template.jte")
        template.optionalValue("name", "world")
        template.conditional("ask", true)

        assertEquals(
            """
            Hello, world!

            How are you?
            I'm good.
            """.trimIndent(),
            template.render(),
        )
    }

    @Test
    fun `for each with no delimiters`() {
        val template = TemplateProcessor("test/for_each_no_delimiters.jte")
        template.iterable("names", listOf("Alice", "Bob", "Charlie"))
        assertEquals("Hello, AliceBobCharlie!", template.render())
    }

    @Test
    fun `for each with delimiters`() {
        val template = TemplateProcessor("test/for_each_with_delimiters.jte")
        template.iterable("names", listOf("Alice", "Bob", "Charlie"))
        assertEquals("Hello, Alice,Bob,Charlie,!", template.render())
    }

    @Test
    fun `multiline for each`() {
        val template = TemplateProcessor("test/multiline_for_each.jte")
        template.iterable("items", listOf("Apples", "Bananas", "Carrots"))
        assertEquals(
            """
            Groceries:
            - Apples
            - Bananas
            - Carrots
            """.trimIndent(),
            template.render(),
        )
    }

    @Test
    fun `empty for each`() {
        val template = TemplateProcessor("test/multiline_for_each.jte")
        template.iterable("items", emptyList())
        assertEquals("Groceries:", template.render())
    }

    @Test
    fun `nested for each`() {
        val template = TemplateProcessor("test/nested_for_each.jte")
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
            template.render(),
        )
    }
}
