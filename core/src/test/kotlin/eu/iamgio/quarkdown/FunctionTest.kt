package eu.iamgio.quarkdown

import kotlin.test.BeforeTest

/**
 * Tests for functions called from a Quarkdown source.
 * For independent function call tests see [StandaloneFunctionTest].
 */
class FunctionTest {
    @BeforeTest
    fun setup() {
        // Throw exception on function call error.
        SystemProperties[SystemProperties.EXIT_ON_ERROR] = ""
    }
}
