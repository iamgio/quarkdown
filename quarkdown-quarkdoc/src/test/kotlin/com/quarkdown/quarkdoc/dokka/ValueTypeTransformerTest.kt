package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for renaming [com.quarkdown.core.function.value.Value] subclasses in Dokka.
 */
class ValueTypeTransformerTest :
    QuarkdocDokkaTest(
        imports =
            listOf(
                NumberValue::class,
                IterableValue::class,
                GeneralCollectionValue::class,
                ObjectValue::class,
                Value::class,
                OutputValue::class,
            ),
    ) {
    @Test
    fun number() {
        test(
            "fun number() = NumberValue(10)",
            "number",
        ) {
            assertContains(getSignature(it), "Number")
            assertFalse("NumberValue" in it)
        }
    }

    @Test
    fun iterable() {
        test(
            "fun iterable(): IterableValue<*> = GeneralCollectionValue(listOf())",
            "iterable",
        ) {
            assertContains(getSignature(it), "Iterable")
            assertFalse("IterableValue" in it)
        }
    }

    @Test
    fun `object`() {
        test(
            "fun obj() = ObjectValue(10)",
            "obj",
        ) {
            val signature = getSignature(it)
            println(signature)
            assertContains(signature, "Int")
            assertTrue(signature.endsWith("Int"))
            assertFalse("ObjectValue" in it)
        }
    }

    @Test
    fun any() {
        test(
            "fun any(): OutputValue<*> = NumberValue(10)",
            "any",
        ) {
            println(getSignature(it))
            assertTrue(getSignature(it).endsWith("Any"))
            assertFalse("OutputValue" in it)
        }
    }

    @Test
    fun `iterable of any`() {
        test(
            "fun iterableOfAny(): IterableValue<OutputValue<*>> = GeneralCollectionValue(listOf())",
            "iterable-of-any",
        ) {
            assertTrue(getSignature(it).endsWith("Iterable<Any>"))
            assertFalse("OutputValue" in it)
        }
    }
}
