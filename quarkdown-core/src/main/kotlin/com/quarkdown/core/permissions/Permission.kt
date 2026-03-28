package com.quarkdown.core.permissions

/**
 * A permission that can be granted to a pipeline, controlling access to certain resources or operations.
 * Permissions are checked at various points during compilation to ensure that a document
 * does not perform unauthorized actions (e.g. reading files outside the project directory or accessing the network).
 * @see PermissionHolder
 * @see MissingPermissionException
 */
sealed interface Permission {
    /**
     * Allows reading files within the project directory.
     */
    data object ProjectRead : Permission

    /**
     * Allows reading files from the entire file system, including outside the project directory.
     */
    data object GlobalRead : Permission

    /**
     * Allows accessing remote resources over the network (e.g. fetching remote media).
     */
    data object NetworkAccess : Permission

    /**
     * Allows using native content features (e.g. embedding raw HTML).
     */
    data object NativeContent : Permission

    companion object {
        /**
         * The default set of permissions granted to a pipeline when no explicit permissions are specified.
         */
        val DEFAULT_SET = setOf(ProjectRead, NativeContent)

        /**
         * The complete set of all available permissions.
         */
        val ALL = setOf(ProjectRead, GlobalRead, NetworkAccess, NativeContent)
    }
}

/**
 * Throws a [MissingPermissionException] if the [required] permission is not present in the [granted] set.
 * @param required the permission that must be present
 * @param granted the set of permissions that have been granted
 * @param message a descriptive message to include in the exception if the permission is missing
 * @throws MissingPermissionException if [required] is not in [granted]
 */
fun requirePermission(
    required: Permission,
    granted: Set<Permission>,
    message: String,
) {
    if (required !in granted) {
        throw MissingPermissionException(message, required)
    }
}
