package com.quarkdown.cli.preview

import com.quarkdown.cli.exec.ExecutionOutcome
import com.quarkdown.core.pipeline.PipelineOptions

/**
 * Strategy that handles delivery of a freshly produced compilation outcome to a preview target
 * after each successful compile, when preview mode is enabled.
 */
interface PreviewStrategy {
    /**
     * Notifies the preview target that a new compilation outcome is available
     * and that the preview should be refreshed accordingly.
     * Implementations are expected to be invoked once per successful compile and may keep internal state.
     * @param options pipeline options used for the compilation that produced [outcome]
     * @param outcome outcome of the compilation that just completed
     */
    fun update(
        options: PipelineOptions,
        outcome: ExecutionOutcome,
    )
}
