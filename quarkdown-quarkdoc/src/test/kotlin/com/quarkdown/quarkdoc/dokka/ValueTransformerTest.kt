package com.quarkdown.quarkdoc.dokka

import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NumberValue
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests for renaming [com.quarkdown.core.function.value.Value] subclasses in Dokka.
 */
class ValueTransformerTest : QuarkdocDokkaTest(imports = listOf(NumberValue::class, IterableValue::class, GeneralCollectionValue::class)) {
    @Test
    fun number() {
        test(
            "fun number() = NumberValue(10)",
            "number",
        ) {
            println(getSignature(it))
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
            println(getSignature(it))
            assertContains(getSignature(it), "Iterable")
            assertFalse("IterableValue" in it)
        }
    }
}
