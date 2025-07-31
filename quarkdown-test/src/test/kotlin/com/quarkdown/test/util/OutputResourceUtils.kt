package com.quarkdown.test.util

import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import kotlin.test.assertIs

/**
 * Given [group] that is an [OutputResourceGroup], returns the set of sub-resources it contains.
 */
fun getSubResources(group: OutputResource?): Set<OutputResource> {
    assertIs<OutputResourceGroup>(group)
    return group.resources
}
