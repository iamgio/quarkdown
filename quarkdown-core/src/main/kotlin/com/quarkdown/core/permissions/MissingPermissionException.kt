package com.quarkdown.core.permissions

import com.quarkdown.core.MISSING_PERMISSION_EXIT_CODE
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Thrown when an operation requires a [Permission] that has not been granted by its [PermissionHolder].
 * @param message a descriptive message explaining the context of the failed permission check
 * @param missingPermission the [Permission] that was required but not granted
 */
class MissingPermissionException(
    message: String,
    missingPermission: Permission,
) : PipelineException(
        richMessage =
            buildInline {
                text("$message: not enough privileges to perform this action. ")
                lineBreak()
                text("Missing required permission: ")
                strong { codeSpan(missingPermission.name) }
                text(". ")
                lineBreak()
                text("To grant this permission, run with: ")
                codeSpan("--allow ${missingPermission.name}")
            },
        code = MISSING_PERMISSION_EXIT_CODE,
    )
