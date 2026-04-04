package com.quarkdown.core.permissions

import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.util.IOUtils
import java.io.File

/**
 * An entity that holds a set of granted [Permission]s and provides convenience methods
 * to check whether specific permissions are available.
 * @see Permission
 */
interface PermissionHolder {
    /**
     * The set of permissions granted to this holder.
     */
    val permissions: Set<Permission>

    /**
     * The root file system used to determine whether a file resides within the project directory.
     * If `null`, all file reads are considered global (requiring [Permission.GlobalRead]).
     */
    val rootFileSystem: FileSystem?
}

/**
 * Throws a [MissingPermissionException] if the [required] permission is not granted to this holder.
 * @param required the permission that must be present
 * @param message a descriptive message to include in the exception if the permission is missing
 * @throws MissingPermissionException if [required] is not granted to this holder
 */
fun PermissionHolder.requirePermission(
    required: Permission,
    message: String,
) {
    requirePermission(required, granted = permissions, message)
}

/**
 * Determines the read permission level required to access the given [file].
 * If the file resides within the project's working directory, [Permission.ProjectRead] is sufficient;
 * otherwise, [Permission.GlobalRead] is required.
 * @param file the file to check
 * @return the required [Permission] to read the file
 */
private fun PermissionHolder.getReadPermission(file: File): Permission {
    val workingDirectory = rootFileSystem?.workingDirectory ?: return Permission.GlobalRead
    return if (IOUtils.isSubPath(workingDirectory, file)) {
        Permission.ProjectRead
    } else {
        Permission.GlobalRead
    }
}

/**
 * Throws a [MissingPermissionException] if the holder does not have the required permission to read the given [file].
 * The required permission depends on whether the file is inside the project directory
 * ([Permission.ProjectRead]) or outside it ([Permission.GlobalRead]).
 * @param file the file to check read access for
 * @param message a descriptive message to include in the exception if the permission is missing
 * @throws MissingPermissionException if the required read permission is not granted to this holder
 * @see getReadPermission
 */
fun PermissionHolder.requireReadPermission(
    file: File,
    message: String = "Cannot access file ${file.absolutePath}",
) {
    requirePermission(required = getReadPermission(file), message)
}
