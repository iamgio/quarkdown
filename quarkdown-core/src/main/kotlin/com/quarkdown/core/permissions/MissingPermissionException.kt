package com.quarkdown.core.permissions

/**
 * Thrown when an operation requires a [Permission] that has not been granted by its [PermissionHolder].
 * @param message a descriptive message explaining the context of the failed permission check
 * @param missingPermission the [Permission] that was required but not granted
 */
class MissingPermissionException(
    message: String,
    missingPermission: Permission,
) : Exception("$message: not enough privileges to perform this action. Missing required permission: $missingPermission")
