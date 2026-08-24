package com.quarkdown.interaction.os

/**
 * Whether Chrome's sandbox must be disabled on [family] for headless Chrome to start.
 *
 * Chrome refuses to run under its sandbox in the container-like environments Quarkdown is commonly
 * used in on Linux, so PDF export disables it there by default. Elsewhere the sandbox stays on, and
 * disabling it is an explicit opt-in.
 *
 * @param family operating system family to check. Defaults to the current one
 */
fun isChromeSandboxUnavailableOn(family: OsFamily = OsUtils.family): Boolean = family == OsFamily.LINUX
