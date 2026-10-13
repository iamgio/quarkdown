package com.quarkdown.core.packages

import com.quarkdown.core.IO_ERROR_EXIT_CODE
import com.quarkdown.core.RUNTIME_ERROR_EXIT_CODE
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Raised when a package reference cannot be turned into files.
 * @param reference the reference as written in the document
 * @param detail what went wrong, in user-facing terms
 * @param cause underlying failure, if any
 */
class PackageResolutionException(
    reference: String,
    detail: String,
    override val cause: Throwable? = null,
) : PipelineException(
        "Cannot resolve package '$reference': $detail",
        IO_ERROR_EXIT_CODE,
    )

/**
 * Raised when a document uses a package reference but the host provides no [PackageResolver].
 */
class UnsupportedPackageResolutionException :
    PipelineException("Package references are not supported in this environment.", RUNTIME_ERROR_EXIT_CODE)
