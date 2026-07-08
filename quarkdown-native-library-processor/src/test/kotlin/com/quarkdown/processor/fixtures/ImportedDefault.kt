@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: a default expression that references a symbol only reachable via the source file's
 * import list (`NoneValue`). If the generator does not carry over the source's imports, the
 * generated wrapper cannot resolve `NoneValue` and the test compilation fails - so this
 * fixture doubles as a build-time check that ImportExtractor works.
 */

@QFunction
fun withImportedDefault(fallback: OutputValue<*> = NoneValue) = VoidValue
