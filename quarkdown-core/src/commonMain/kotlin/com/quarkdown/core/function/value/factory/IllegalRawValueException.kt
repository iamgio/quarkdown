package com.quarkdown.core.function.value.factory

import com.quarkdown.core.ILLEGAL_TYPE_CONVERSION_EXIT_CODE
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * An exception thrown when a dynamic value cannot be converted to a static type via a [ValueFactory] method.
 *
 * Subclasses describe a particular way a conversion can fail. Being a common supertype is what lets
 * [ValueFactory.tryOrNull] turn any rejected conversion into `null`.
 *
 * @param fullMessage the complete message, for subclasses that phrase the failure their own way
 *                    rather than as a reason followed by the offending value
 */
open class IllegalRawValueException protected constructor(
    fullMessage: String,
) : PipelineException(
        fullMessage,
        ILLEGAL_TYPE_CONVERSION_EXIT_CODE,
    ) {
    /**
     * @param message reason the value was rejected
     * @param raw raw value that could not be converted
     */
    constructor(message: String, raw: Any) : this("$message: $raw")
}
