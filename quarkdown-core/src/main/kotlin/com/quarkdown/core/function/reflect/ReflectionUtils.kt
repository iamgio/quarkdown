package com.quarkdown.core.function.reflect

import kotlin.reflect.full.declaredMembers

/**
 * General utilities for Kotlin reflection.
 */
object ReflectionUtils {
    /**
     * @param name name of the constant, case-insensitive
     * @param T class to extract the constant from **and** the value type
     * @return value of the constant with the given name if found, `null` otherwise
     */
    inline fun <reified T> getConstantByName(name: String): T? =
        T::class.declaredMembers.find { it.name.equals(name, ignoreCase = true) }?.call() as? T
}
