package com.quarkdown.core.filesystem

/**
 * @return the file system a pipeline uses when none is supplied:
 *         the physical disk where one exists, an empty in-memory one otherwise
 */
expect fun defaultFileSystem(): FileSystem
