package com.quarkdown.server.stop

import io.ktor.server.application.Application

/**
 * Adapter for [Stoppable] of a Ktor application.
 */
class KtorStoppableAdapter(
    private val application: Application,
) : Stoppable {
    override fun stop() {
        application.engine.stop()
    }
}
