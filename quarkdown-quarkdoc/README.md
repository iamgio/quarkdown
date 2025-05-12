# quarkdoc

This module contains the Quarkdoc plugin for [Dokka](https://github.com/Kotlin/dokka), the Kotlin documentation engine.

Quarkdoc extends Dokka by providing Quarkdown-level documentation for native libraries,
i.e. collections of strongly-typed Quarkdown functions written in Kotlin.

When a module adopts this plugin, its benefits include:

- Quarkdown-syntax function signatures; 
- Package generation from Quarkdown modules: in a native Quarkdown library, a `Module` is a single source file.
  The plugin generates a pseudo-package for each module, making it easier to navigate library functions by module name; 
- Function/parameter name adaptation via `@Name` (Quarkdown's functions don't always match their native signature);
- Listing of enum entries for enum-type function parameters;
- Function's document type constraints via `@OnlyForDocumentType`/`@NotForDocumentType`;
- Suppression of `@Injected` function parameters;

To see all enhancements, [`QuarkdocDokkaPlugin`](src/main/kotlin/com/quarkdown/quarkdoc/dokka/QuarkdocDokkaPlugin.kt) features a complete list.