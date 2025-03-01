package eu.iamgio.quarkdown.cli.creator.content

import eu.iamgio.quarkdown.pipeline.output.OutputResource

interface ProjectCreatorInitialContentSupplier {
    val templateCodeContent: String?

    fun createResources(): Set<OutputResource>
}
