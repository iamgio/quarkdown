package eu.iamgio.quarkdown.function.value

/**
 * A [Value] that wraps an [Iterable] collection of [Value] elements.
 * @param T type of the elements
 * @see OrderedCollectionValue
 * @see UnorderedCollectionValue
 * @see GeneralCollectionValue
 * @see PairValue
 */
interface IterableValue<T : OutputValue<*>> : InputValue<Iterable<T>>, OutputValue<Iterable<T>>, Destructurable<T>, Iterable<T> {
    override val unwrappedValue: Iterable<T>

    override fun iterator(): Iterator<T> = unwrappedValue.iterator()

    override val destructurableComponents: List<T>
        get() = unwrappedValue.toList()
}
