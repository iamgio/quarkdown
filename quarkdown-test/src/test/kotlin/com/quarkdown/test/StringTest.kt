package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for string manipulation.
 */
class StringTest {
    @Test
    fun `explicit string`() {
        execute(".string {}") {
            assertEquals("<p></p>", it)
        }

        execute(".string {hello}") {
            assertEquals("<p>hello</p>", it)
        }

        execute(".string { hello }") {
            assertEquals("<p>hello</p>", it)
        }

        execute(".string {\" hello \"}") {
            assertEquals("<p> hello </p>", it)
        }
    }

    @Test
    fun case() {
        execute(".uppercase {hello}") {
            assertEquals("<p>HELLO</p>", it)
        }

        execute(".lowercase {HELLO}") {
            assertEquals("<p>hello</p>", it)
        }

        execute(".capitalize {hello}") {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun concatenation() {
        execute(".concatenate {hello} {world}") {
            assertEquals("<p>helloworld</p>", it)
        }

        execute(".concatenate {hello} with:{ world}") {
            assertEquals("<p>helloworld</p>", it)
        }

        execute(".concatenate {hello} {.string {\" world\"}}") {
            assertEquals("<p>hello world</p>", it)
        }

        execute(".concatenate {hello} {world} if:{true}") {
            assertEquals("<p>helloworld</p>", it)
        }

        execute(".concatenate {hello} {world} if:{no}") {
            assertEquals("<p>hello</p>", it)
        }
    }

    @Test
    fun `split with default separator`() {
        execute(".split {A, B, C}::size") {
            assertEquals("<p>3</p>", it)
        }
        execute(".split {A, B, C}::second") {
            assertEquals("<p>B</p>", it)
        }
    }

    @Test
    fun `split with custom separator`() {
        execute(".var {date} {2026-10-24}\n\n.date::split separator:{-}::first") {
            assertEquals("<p>2026</p>", it)
        }
    }

    @Test
    fun `split round trips with join`() {
        execute(".split {A, B, C}::join") {
            assertEquals("<p>A, B, C</p>", it)
        }
    }

    @Test
    fun `split without separator occurrences is a single part`() {
        execute(".split {ABC}::size") {
            assertEquals("<p>1</p>", it)
        }
    }

    @Test
    fun `split of empty string is an empty collection`() {
        execute(".split {}::size") {
            assertEquals("<p>0</p>", it)
        }
    }

    @Test
    fun `starts with`() {
        execute(".startswith {hello world} {hello}") {
            assertEquals("<p><input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
        }

        execute(".sum {3} {8}::startswith {1}") {
            assertEquals("<p><input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
        }
    }
}
