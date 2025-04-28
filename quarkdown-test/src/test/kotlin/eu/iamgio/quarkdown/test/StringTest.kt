package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
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
}
