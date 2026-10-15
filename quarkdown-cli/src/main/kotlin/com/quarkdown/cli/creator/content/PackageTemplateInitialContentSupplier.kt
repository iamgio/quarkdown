package com.quarkdown.cli.creator.content

import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.toOutputResources
import com.quarkdown.template.TemplateProcessor

/**
 * A [ProjectCreatorInitialContentSupplier] backed by a package that declares itself a project template:
 * the template's content is injected verbatim into the main file, and its additional files
 * are copied into the project at their package-relative paths, names kept as they are.
 * @param template the resolved template
 */
class PackageTemplateInitialContentSupplier(
    private val template: PackageTemplate,
) : ProjectCreatorInitialContentSupplier {
    override fun createInitialContent(template: TemplateProcessor): CharSequence = this.template.content

    override fun createResources(): Set<OutputResource> = template.additionalFiles.toOutputResources()
}
