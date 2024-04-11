package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.UNRESOLVED_REFERENCE_EXIT_CODE
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 *
 */
class UnresolvedReferenceException(symbol: String) : PipelineException("Unresolved reference: $symbol", UNRESOLVED_REFERENCE_EXIT_CODE)
