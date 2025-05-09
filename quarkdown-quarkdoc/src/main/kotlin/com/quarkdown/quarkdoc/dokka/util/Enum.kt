package com.quarkdown.quarkdoc.dokka.util

import org.jetbrains.dokka.model.EnumValue

/**
 * Converts an [EnumValue] to an enum of type [E] using the provided [valueOf] function.
 * @param valueOf a function that converts an enum name to an enum of type [E]
 * @return the enum of type [E] corresponding to the [EnumValue]
 */
fun <E : Enum<*>> EnumValue.toEnum(valueOf: (String) -> E): E = valueOf(enumName.substringAfterLast('.'))
