package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.media.storage.options.MediaStorageOptions
import eu.iamgio.quarkdown.media.storage.options.ReadOnlyMediaStorageOptions
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler
import java.io.File

/**
 * Read-only settings that affect different behaviors of a [Pipeline].
 * @param prettyOutput whether the rendering stage should produce pretty output code
 * @param wrapOutput whether the rendered code should be wrapped in a template code.
 * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`,
 * with the actual content injected in `body`
 * @param workingDirectory the starting directory to use when resolving relative paths from function calls
 * @param enableMediaStorage whether media storage should be enabled.
 * If enabled, media objects referenced in the document are copied to the output directory
 * and those elements that use them (e.g. images) automatically reference the new local path.
 * This doesn't take effect with the base Markdown flavor,
 * as the media architecture is defined by Quarkdown through a [eu.iamgio.quarkdown.context.hooks.MediaStorerHook].
 * If this is disabled, [MediaStorageOptions] are ignored.
 * @param mediaStorageOptionsOverrides rules that override the default behavior of the media storage system
 * @param errorHandler the error handler strategy to use when an error occurs in the pipeline, during the processing of a Quarkdown file
 */
data class PipelineOptions(
    val prettyOutput: Boolean = false,
    val wrapOutput: Boolean = true,
    val workingDirectory: File? = null,
    val enableMediaStorage: Boolean = true,
    val mediaStorageOptionsOverrides: MediaStorageOptions = ReadOnlyMediaStorageOptions(),
    val errorHandler: PipelineErrorHandler = BasePipelineErrorHandler(),
)
