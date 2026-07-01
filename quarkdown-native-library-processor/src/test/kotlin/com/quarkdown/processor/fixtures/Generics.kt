@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: generic types, nullable types, and multi-argument generics.
 * Exercises [com.quarkdown.processor.generation.KSTypeRenderer] through the generator.
 */

@QFunction
fun genericTypes(
    items: List<String>,
    nullable: String?,
    mapping: Map<String, Int>,
    nested: List<List<String>>,
) = VoidValue
