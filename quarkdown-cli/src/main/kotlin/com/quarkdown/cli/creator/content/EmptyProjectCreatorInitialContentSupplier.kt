package com.quarkdown.cli.creator.content

import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.template.TemplateProcessor

/**
 * A [ProjectCreatorInitialContentSupplier] that provides no initial content or resources.
 */
class EmptyProjectCreatorInitialContentSupplier : ProjectCreatorInitialContentSupplier {
    override fun createInitialContent(template: TemplateProcessor): CharSequence? = null

    override fun createResources(): Set<OutputResource> = emptySet()
}
