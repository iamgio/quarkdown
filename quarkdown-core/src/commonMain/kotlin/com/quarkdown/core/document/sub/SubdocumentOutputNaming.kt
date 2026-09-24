package com.quarkdown.core.document.sub

import com.quarkdown.core.context.Context

/**
 * Strategy for naming subdocument output files.
 * @see Subdocument.getOutputFileName
 */
enum class SubdocumentOutputNaming {
    /**
     * Uses the subdocument's file name.
     */
    FILE_NAME,

    /**
     * Uses a hash-based unique name to minimize collisions.
     */
    COLLISION_PROOF,

    /**
     * Uses the document name set via `.docname`, falling back to [FILE_NAME] if unset.
     */
    DOCUMENT_NAME,
}

/**
 * Returns the output file name for the subdocument, based on the context's [SubdocumentOutputNaming] strategy.
 * @param context the context that holds the pipeline options
 * @return the output file name for the subdocument
 * @see com.quarkdown.core.pipeline.PipelineOptions.subdocumentNaming
 */
fun Subdocument.getOutputFileName(context: Context): String =
    when (context.attachedPipeline?.options?.subdocumentNaming) {
        SubdocumentOutputNaming.COLLISION_PROOF -> uniqueName
        SubdocumentOutputNaming.DOCUMENT_NAME -> context.documentInfo.name ?: name
        else -> name
    }
