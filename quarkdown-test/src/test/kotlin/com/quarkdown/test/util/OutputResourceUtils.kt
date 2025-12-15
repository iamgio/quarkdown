package com.quarkdown.test.util

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.media.storage.MEDIA_SUBDIRECTORY_NAME
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import kotlin.test.assertIs
import kotlin.test.assertNotNull

const val INDEX = "index"

/**
 * Given [group] that is an [OutputResourceGroup], returns the set of sub-resources it contains.
 */
fun getSubResources(group: OutputResource?): Set<OutputResource> {
    assertIs<OutputResourceGroup>(group)
    return group.resources
}

/**
 * Given [group] that is an [OutputResourceGroup], retrieves the media resources inside the
 * `media/` subdirectory.
 */
fun getMediaResources(
    group: OutputResource?,
    subdocumentName: String? = null,
): Set<OutputResource> {
    val subdocumentGroup =
        if (subdocumentName != null) {
            getSubdocumentGroup(group, subdocumentName)
        } else {
            group
        }

    val mediaGroup = getSubResources(subdocumentGroup).find { it.name == MEDIA_SUBDIRECTORY_NAME }
    assertIs<OutputResourceGroup>(mediaGroup)
    return mediaGroup.resources
}

fun getSubdocumentGroup(
    group: OutputResource?,
    name: String,
): OutputResourceGroup {
    val resources = getSubResources(group)
    val subdocumentGroup = resources.firstOrNull { it.name == name } as? OutputResourceGroup

    assertNotNull(subdocumentGroup)
    return subdocumentGroup
}

fun getSubdocumentGroup(
    group: OutputResource?,
    subdocument: Subdocument,
    context: Context,
): OutputResourceGroup {
    val subdocumentGroup =
        if (subdocument == Subdocument.Root) {
            group as OutputResourceGroup
        } else {
            getSubdocumentGroup(group, subdocument.getOutputFileName(context))
        }
    return subdocumentGroup
}

/**
 * Retrieves the resource for the given [subdocument] from the [group].
 *
 * - If [subdocument] is the root, retrieves `index.html`.
 * - Otherwise, retrieves the `index.html` from the corresponding subdocument group.
 */
fun getSubdocumentResource(
    group: OutputResource?,
    subdocument: Subdocument,
    context: Context,
): TextOutputArtifact {
    val subdocumentGroup = getSubdocumentGroup(group, subdocument, context)
    val resource = subdocumentGroup.resources.first { it.name == INDEX } as? TextOutputArtifact
    assertNotNull(resource)
    return resource
}

/**
 * Counts how many subdocument resources are present in [group].
 * A subdocument resource is either an `index.html` at the root of [group],
 * or an `index.html` inside a subdocument group.
 */
fun getSubdocumentResourceCount(group: OutputResource?): Int =
    getSubResources(group)
        .count {
            it.name == INDEX || (it is OutputResourceGroup && it.resources.any { res -> res.name == INDEX })
        }
