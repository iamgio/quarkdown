package com.quarkdown.core.function.value

/**
 * Anything that can be destructured into a sequence of [Value] components.
 *
 * A common usage is with an [IterableValue], which can be destructured into its elements.
 * For example, a [PairValue] is an [IterableValue] that can be destructured into its two components.
 *
 * Destructuring in Quarkdown may occur, for example, in `.foreach`.
 *
 * @param T type of the components
 * @see IterableValue
 */
interface Destructurable<T : Value<*>> {
    /**
     * Components that can be the result of a destructuring operation.
     */
    val destructurableComponents: List<T>

    /**
     * Destructures this object into a list of components.
     * @param componentCount number of components to destructure
     * @return a list of components of size [componentCount]
     * @throws IllegalArgumentException if [componentCount] is greater than the number of available components
     */
    fun destructured(componentCount: Int): List<T> {
        // Ensuring the iterable has enough components to destructure.
        if (componentCount > destructurableComponents.size) {
            throw IllegalArgumentException(
                "Cannot destructure value: $destructurableComponents. " +
                    "The value has ${destructurableComponents.size} components, but $componentCount were requested.",
            )
        }

        return destructurableComponents.take(componentCount)
    }
}
