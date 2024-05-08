package eu.iamgio.quarkdown

/**
 * Exit code when a source file was not supplied.
 * @depecated REPL mode is now used in the `cli` module if no source file is supplied. Keeping this for possible future uses.
 */
const val NO_SOURCE_FILE_EXIT_CODE = 64

/**
 * Exit code when a compile-time error occurs.
 */
const val COMPILE_ERROR_EXIT_CODE = 65

/**
 * Exit code when a Quarkdown function was invoked by an incompatible call.
 */
const val BAD_FUNCTION_CALL_EXIT_CODE = 66

/**
 * Exit code when a Quarkdown function can't be resolved.
 */
const val UNRESOLVED_REFERENCE_EXIT_CODE = 67

/**
 * Exit code when an element (e.g. an enum value from a Quarkdown function argument)
 * does not exist in a look-up table.
 */
const val NO_SUCH_ELEMENT_EXIT_CODE = 68

/**
 * Exit code when a I/O error occurs.
 */
const val IO_ERROR_EXIT_CODE = 69

/**
 * Exit code when a runtime error occurs.
 */
const val RUNTIME_ERROR_EXIT_CODE = 70
