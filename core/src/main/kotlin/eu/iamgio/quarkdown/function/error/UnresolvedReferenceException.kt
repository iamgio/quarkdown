package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.UNRESOLVED_REFERENCE_EXIT_CODE
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * An exception thrown when a function call does not reference any registered function declaration.
 * @param symbol function name
 */
class UnresolvedReferenceException(symbol: String) : PipelineException("Unresolved reference: $symbol", UNRESOLVED_REFERENCE_EXIT_CODE)
