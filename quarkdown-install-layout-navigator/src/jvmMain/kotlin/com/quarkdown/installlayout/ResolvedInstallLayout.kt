package com.quarkdown.installlayout

private val resolvedLayout: InstallLayout by lazy(InstallDirectoryResolver::resolve)

private val resolvedLayoutOrNull: InstallLayout? by lazy { runCatching { resolvedLayout }.getOrNull() }

/**
 * The install layout this JVM process runs from, resolved lazily once from the location of the running JAR.
 * @throws IllegalStateException if the install directory cannot be resolved
 */
val InstallLayout.Companion.get: InstallLayout
    get() = resolvedLayout

/**
 * [get], or `null` if the install directory cannot be resolved.
 */
val InstallLayout.Companion.getOrNull: InstallLayout?
    get() = resolvedLayoutOrNull
