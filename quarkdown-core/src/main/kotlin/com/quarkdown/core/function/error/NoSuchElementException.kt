package com.quarkdown.core.function.error

import com.quarkdown.core.NO_SUCH_ELEMENT_EXIT_CODE
import com.quarkdown.core.function.value.quarkdownName
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Exception thrown when an element (e.g. an enum value from a Quarkdown function argument)
 * does not exist among elements of a look-up table.
 */
class NoSuchElementException(element: Any, values: Iterable<*>) :
    PipelineException("No such element '$element' among values $values", NO_SUCH_ELEMENT_EXIT_CODE) {
    constructor(element: Any, values: Array<Enum<*>>) : this(element, values.map { it.quarkdownName })
}
