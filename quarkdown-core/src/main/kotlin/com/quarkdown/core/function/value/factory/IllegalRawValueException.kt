package com.quarkdown.core.function.value.factory

import com.quarkdown.core.ILLEGAL_TYPE_CONVERSION_EXIT_CODE
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * An exception thrown when a dynamic value cannot be converted to a static type via a [ValueFactory] method.
 * @param raw raw value that could not be converted
 */
class IllegalRawValueException(
    message: String,
    raw: Any,
) : PipelineException(
        "$message: $raw",
        ILLEGAL_TYPE_CONVERSION_EXIT_CODE,
    )
