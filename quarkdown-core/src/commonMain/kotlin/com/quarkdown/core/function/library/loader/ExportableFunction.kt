package com.quarkdown.core.function.library.loader

import com.quarkdown.core.function.Function

/**
 * A Quarkdown function that can be exported in a [com.quarkdown.core.function.library.module.QuarkdownModule].
 *
 * Native functions are built at compile time by the native library processor, which emits one
 * ready-made [Function] per `@QFunction`.
 */
typealias ExportableFunction = Function<*>
