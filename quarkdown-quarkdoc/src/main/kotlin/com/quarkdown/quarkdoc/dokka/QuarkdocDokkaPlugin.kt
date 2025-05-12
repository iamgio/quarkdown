package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.page.DocumentTypeConstraintsPageTransformer
import com.quarkdown.quarkdoc.dokka.signature.QuarkdownSignatureProvider
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.EnumParameterEntryListerTransformer
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.EnumStorer
import com.quarkdown.quarkdoc.dokka.transformers.misc.DocumentTypeConstraintsTransformer
import com.quarkdown.quarkdoc.dokka.transformers.module.ModuleAsPackageTransformer
import com.quarkdown.quarkdoc.dokka.transformers.module.ModulesStorer
import com.quarkdown.quarkdoc.dokka.transformers.name.DocumentableNameTransformer
import com.quarkdown.quarkdoc.dokka.transformers.name.DocumentationNameTransformer
import com.quarkdown.quarkdoc.dokka.transformers.name.RenamingsStorer
import com.quarkdown.quarkdoc.dokka.transformers.suppress.SuppressInjectedTransformer
import com.quarkdown.quarkdoc.dokka.transformers.type.ValueTypeTransformer
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

/**
 * Dokka plugin that generates ad-hoc documentation for native Quarkdown functions.
 */
@Suppress("unused")
class QuarkdocDokkaPlugin : DokkaPlugin() {
    private val base by lazy { plugin<DokkaBase>() }

    /**
     * Stores the modules in which the functions are declared, to be used in [moduleAsPackageTransformer].
     * @see com.quarkdown.core.function.library.loader.Module
     */
    val modulesStorer by extending {
        base.preMergeDocumentableTransformer providing ::ModulesStorer order { before(moduleAsPackageTransformer) }
    }

    /**
     * Quarkdown modules, defined by a [com.quarkdown.core.function.library.loader.Module] property,
     * contain the functions declared in the same source file and are shown in the documentation as packages.
     * @see com.quarkdown.core.function.library.loader.Module
     */
    val moduleAsPackageTransformer by extending {
        base.preMergeDocumentableTransformer providing ::ModuleAsPackageTransformer
    }

    /**
     * Stores the old-new function name pairs, to be used in [documentableNameTransformer] and [documentationNameTransformer].
     * @see com.quarkdown.core.function.reflect.annotation.Name
     */
    val renamingsStorer by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::RenamingsStorer order {
            before(documentableNameTransformer, documentationNameTransformer)
        }
    }

    /**
     * Functions and parameters annotated with `@Name` are renamed in the function signature.
     * @see com.quarkdown.core.function.reflect.annotation.Name
     */
    val documentableNameTransformer by extending {
        base.preMergeDocumentableTransformer providing ::DocumentableNameTransformer
    }

    /**
     * Functions and parameters annotated with `@Name` are renamed in the documentation.
     * This includes:
     * - Direct links (`[name]`)
     * - Parameter (`@param name`)
     * - See references (`@see name`)
     * @see com.quarkdown.core.function.reflect.annotation.Name
     */
    val documentationNameTransformer by extending {
        base.preMergeDocumentableTransformer providing ::DocumentationNameTransformer
    }

    /**
     * Renames references of [com.quarkdown.core.function.value.Value], and subclasses, in the signature
     * to a more human-readable form.
     */
    val valueTypeTransformer by extending {
        base.preMergeDocumentableTransformer providing ::ValueTypeTransformer
    }

    /**
     * Parameters annotated with `@Injected` are hidden (suppressed) in the generated documentation.
     * @see com.quarkdown.core.function.reflect.annotation.Injected
     */
    val suppressInjectedTransformer by extending {
        base.preMergeDocumentableTransformer providing ::SuppressInjectedTransformer
    }

    /**
     * Stores enum declarations, to be used in [enumParameterEntryListerTransformer].
     */
    val enumStorer by extending {
        base.preMergeDocumentableTransformer providing ::EnumStorer order {
            before(enumParameterEntryListerTransformer)
        }
    }

    /**
     * Lists enum entries in the documentation for parameters that expect an enum.
     */
    val enumParameterEntryListerTransformer by extending {
        base.preMergeDocumentableTransformer providing ::EnumParameterEntryListerTransformer
    }

    /**
     * Given a function annotated with `@OnlyForDocumentType` which defines constraints
     * about the document type the function supports, this transformer stores this data
     * for [documentTypeConstraintsPageTransformer] to display it.
     * @see com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
     */
    val documentPositiveTypeConstraintsTransformer by extending {
        base.preMergeDocumentableTransformer providing DocumentTypeConstraintsTransformer::Positive
    }

    /**
     * Like [documentPositiveTypeConstraintsTransformer] but for the negative case, via `@NotForDocumentType`.
     * @see com.quarkdown.core.function.reflect.annotation.NotForDocumentType
     */
    val documentNegativeTypeConstraintsTransformer by extending {
        base.preMergeDocumentableTransformer providing DocumentTypeConstraintsTransformer::Negative
    }

    /**
     * Displays the document type constraints produced by [documentTypeConstraintsTransformer] in the documentation.
     */
    val documentTypeConstraintsPageTransformer by extending {
        CoreExtensions.pageTransformer providing ::DocumentTypeConstraintsPageTransformer
    }

    /**
     * Generates Quarkdown signatures for functions in Quarkdown modules.
     */
    val signatureProvider by extending {
        base.signatureProvider providing ::QuarkdownSignatureProvider override base.kotlinSignatureProvider
    }

    @DokkaPluginApiPreview
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}
