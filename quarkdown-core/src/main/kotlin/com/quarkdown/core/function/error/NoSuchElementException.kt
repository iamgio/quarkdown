package com.quarkdown.core.function.error

import com.quarkdown.core.function.quarkdownName
import com.quarkdown.core.function.value.factory.IllegalRawValueException

/**
 * Exception thrown when an element (e.g. an enum value from a Quarkdown function argument)
 * does not exist among elements of a look-up table.
 *
 * A conversion failure like any other, so it is caught, reported and exited on as one.
 *
 * @param element the value that matched nothing
 * @param values the elements it was matched against
 */
class NoSuchElementException(
    element: Any,
    values: Iterable<*>,
) : IllegalRawValueException("No such element '$element' among values $values") {
    constructor(element: Any, values: Array<Enum<*>>) : this(element, values.map { it.quarkdownName })
}
