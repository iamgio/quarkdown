package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.NO_SUCH_ELEMENT_EXIT_CODE
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * Exception thrown when an element (e.g. an enum value from a Quarkdown function argument)
 * does not exist among elements of a look-up table.
 */
class NoSuchElementFunctionException(element: Any, values: Iterable<*>) :
    PipelineException("No such element '$element' among values $values", NO_SUCH_ELEMENT_EXIT_CODE) {
    constructor(element: Any, values: Array<Enum<*>>) : this(element, values.map { it.name.lowercase() })
}
