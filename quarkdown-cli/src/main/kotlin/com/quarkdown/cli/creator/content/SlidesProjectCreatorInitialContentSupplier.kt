package com.quarkdown.cli.creator.content

import com.quarkdown.template.TemplateProcessor

private const val CODE_TEMPLATE_NAME = "creator/slides/initialcontent.qd.jte"

/**
 * A [ProjectCreatorInitialContentSupplier] that provides initial content for `slides` projects:
 * a presentation-oriented snippet with a page footer, a title slide, and a couple of example slides,
 * along with the same image assets supplied by [DefaultProjectCreatorInitialContentSupplier].
 */
class SlidesProjectCreatorInitialContentSupplier :
    ProjectCreatorInitialContentSupplier by DefaultProjectCreatorInitialContentSupplier() {
    override fun createInitialContent(template: TemplateProcessor) = template.renderInitialContent(CODE_TEMPLATE_NAME)
}
