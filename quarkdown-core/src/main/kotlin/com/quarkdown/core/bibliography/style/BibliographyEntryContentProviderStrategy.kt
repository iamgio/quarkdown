package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.BibliographyEntryVisitor

/**
 * Supplier of rich content for bibliography entries.
 */
interface BibliographyEntryContentProviderStrategy : BibliographyEntryVisitor<InlineContent>
