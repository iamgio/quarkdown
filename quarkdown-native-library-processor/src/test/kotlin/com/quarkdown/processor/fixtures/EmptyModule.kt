@file:QModule

package com.quarkdown.processor.fixtures

import com.quarkdown.processor.annotation.QModule

/*
 * Fixture: `@file:QModule` with no `@QFunction`. The processor should warn and still emit an
 * object with an empty `moduleOf(...)` call.
 */
