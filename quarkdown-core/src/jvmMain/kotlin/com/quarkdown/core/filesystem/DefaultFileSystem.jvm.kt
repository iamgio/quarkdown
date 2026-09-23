package com.quarkdown.core.filesystem

actual fun defaultFileSystem(): FileSystem = DiskFileSystem()
