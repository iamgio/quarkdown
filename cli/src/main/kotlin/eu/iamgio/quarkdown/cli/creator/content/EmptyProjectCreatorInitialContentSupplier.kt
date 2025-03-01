package eu.iamgio.quarkdown.cli.creator.content

import eu.iamgio.quarkdown.pipeline.output.OutputResource

class EmptyProjectCreatorInitialContentSupplier : ProjectCreatorInitialContentSupplier {
    override val templateCodeContent: String? = null

    override fun createResources(): Set<OutputResource> = emptySet()
}
