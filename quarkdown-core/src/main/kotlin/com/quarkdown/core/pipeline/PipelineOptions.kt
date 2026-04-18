package com.quarkdown.core.pipeline

import com.quarkdown.core.document.sub.SubdocumentOutputNaming
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.core.pipeline.error.PipelineErrorHandler
import java.io.File

/**
 * Read-only settings that affect different behaviors of a [Pipeline].
 * @param resourceName name of the output resource, that overrides the value of `.docname` set in the document
 * @param prettyOutput whether the rendering stage should produce pretty output code
 * @param wrapOutput whether the rendered code should be wrapped in a template code.
 * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`,
 * with the actual content injected in `body`
 * @param workingDirectory the starting directory to use when resolving relative paths from function calls.
 * Note: subdocuments may have different working directories. For consistent results rely on [com.quarkdown.core.context.file.FileSystem.workingDirectory]
 * @param enableMediaStorage whether media storage should be enabled.
 * If enabled, media objects referenced in the document are copied to the output directory
 * and those elements that use them (e.g. images) automatically reference the new local path.
 * This doesn't take effect with the base Markdown flavor,
 * as the media architecture is defined by Quarkdown through a [com.quarkdown.core.context.hooks.MediaStorerHook].
 * If this is disabled, [MediaStorageOptions] are ignored.
 * @param subdocumentNaming the strategy used to determine subdocument output file names
 * @param isPreview whether the pipeline is running in preview mode, which may suppress certain post-rendering steps, such as HTML sitemap generation
 * @param permissions the set of permissions granted to this pipeline, controlling access to file system, network, and other resources
 * @param mediaStorageOptionsOverrides rules that override the default behavior of the media storage system
 * @param errorHandler the error handler strategy to use when an error occurs in the pipeline, during the processing of a Quarkdown file
 * @param serverPort port to communicate with the local server on. If not set, no server communication is performed. In a practical scenario,
 *                   this is injected into JavaScript to communicate with the server, for example to enable dynamic reloading.
 */
data class PipelineOptions(
    val resourceName: String? = null,
    val prettyOutput: Boolean = false,
    val wrapOutput: Boolean = true,
    val workingDirectory: File? = null,
    val enableMediaStorage: Boolean = true,
    val subdocumentNaming: SubdocumentOutputNaming = SubdocumentOutputNaming.FILE_NAME,
    val isPreview: Boolean = false,
    val serverPort: Int? = null,
    val permissions: Set<Permission> = Permission.DEFAULT_SET,
    val mediaStorageOptionsOverrides: MediaStorageOptions = ReadOnlyMediaStorageOptions(),
    val errorHandler: PipelineErrorHandler = BasePipelineErrorHandler(),
)
