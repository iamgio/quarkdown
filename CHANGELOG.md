# Changelog

## [Unreleased]

This version is the biggest release to date, with a large number of new features and improvements, and a [new official wiki](https://quarkdown.com/wiki), written in Quarkdown, that fully replaces the GitHub wiki for a better experience.

> Going forward, next minor releases will be smaller and more frequent.

### Added

#### [`docs` document type](https://quarkdown.com/wiki/document-types#docs-docs)

`docs` is the fourth document type available in Quarkdown, alongside `plain`, `paged` and `slides`. It is designed for technical documentation, wikis and knowledge bases.

It derives from `plain`, and adds a customizable navigation sidebar, a ToC sidebar, a header, accurate client-side search, and next/previous page navigation buttons.

You can see it in action in the [new official wiki](https://quarkdown.com/wiki)! To get started with a new `docs` document, you can rely on `quarkdown create` as usual.

#### New themes: Galactic (color) and Hyperlegible (layout)

This new theme combination is the one used in the new wiki for improved readability and modern look.

<img width="606" height="350" alt="Galactic+Hyperlegible" src="https://github.com/user-attachments/assets/d8609d22-fb57-4db9-8b4a-98fa28cda421" />

#### [GitHub-style alerts](https://quarkdown.com/wiki/quote-types)

GitHub's alert syntax is now supported, making it easier to migrate from other tools:

```markdown
> [!NOTE]
> This is a note
```

Note that Quarkdown's original syntax is still supported *and recommended*, especially for English documents:

```markdown
> Note: This is a note
```

#### [Subdocument links now allow anchors](https://quarkdown.com/wiki/subdocuments)

Links to Quarkdown subdocuments now support anchors, to link to specific sections:

```markdown
[Page](page.qd#section)
```

#### [Customizable page numbering format](https://quarkdown.com/wiki/page-counter#formatting-the-page-number)

The `.formatpagenumber {format}` function overrides the page numbering format from the current page onward. It accepts the same format specifiers as `.numbering`, and applies to both page counters and table of contents.

```markdown
.pagemargin {topcenter}
    .currentpage

# First page

.formatpagenumber {i}

# Second page

# Third page
```

<img width="550" alt="Page number format example" src="https://raw.githubusercontent.com/iamgio/quarkdown/main/docs/page-counter/format.png" />

Thanks @OverSamu!

#### [Horizontal/vertical gap customization of `.grid`](https://quarkdown.com/wiki/stacks#parameters)

The `.grid` function now accepts `hgap` and `vgap` parameters to customize the horizontal and vertical gaps between grid items. `gap` still works as a shorthand for both.

Thanks @OverSamu!

#### [`none` is now converted to `null`](https://quarkdown.com/wiki/none#passing-none-to-functions)

When invoking a native function from the stdlib, [`none`](https://quarkdown.com/wiki/none) is now supported by nullable parameters, and converted to `null`.

Before:

```markdown
.function {rectangle}
    width height background?:
    .if {.background::isnone}
        .container width:{.width} height:{.height}
    .ifnot {.background::isnone}
        .container width:{.width} height:{.height} background:{.background}
```

After:

```markdown
.function {rectangle}
    width height background?:
    .container width:{.width} height:{.height} background:{.background}
```

#### [Icons](https://quarkdown.com/wiki/icons)

The new `.icon {name}` function relies on [Bootstrap Icons](https://icons.getbootstrap.com/#icons) to display pixel-perfect icons in your documents.

```markdown
Quarkdown is on .icon {github}
```

#### New output target: plain text

Quarkdown can now render to plain text (`.txt`) via `--render plaintext`.

This has no particular use case. It was needed to implement the docs search feature in the first place.

#### Get path to root directory

The new `.pathtoroot {granularity?}` function returns the relative path from the current source file to the parent directory of:
- the root document, if `granularity` is `project` (default)
- the subdocument, if `granularity` is `subdocument`

### Changed

#### `.css` doesn't require `!important` anymore

The `.css` function now applies `!important` automatically at the end of each rule.

#### Revised navigation sidebar

The navigation sidebar, visible in `plain` and `paged` documents on web view, is now easier to navigate, with all entries visible at once, and more accessible for screen readers.

<img width="208" height="632" alt="Sidebar" src="https://github.com/user-attachments/assets/54956e66-db34-486f-bb9a-275271ae7a7e" />

Additionally, its generation is now performed at compile time rather than runtime, providing major performance improvements for large documents.

#### Flexible naming strategy for subdocument output files

`--no-subdoc-collisions` was removed in favor of `--subdoc-naming <strategy>`, which is a flexible way to choose how subdocument output files are named:

- `file-name` (default): each subdocument output file is named after its source file
- `document-name`: each subdocument output file is named after its `.docname` value
- `collision-proof`: former `--no-subdoc-collisions`

#### Revamped `create` CLI

The `quarkdown create` command is now more intuitive, for a smoother onboarding experience.

#### Libraries now include content

`.include {library}` now also includes top-level Markdown content from the library, just like `.include {file.qd}` does for regular files.

#### Page content border adjustments

Page content border (`.pageformat bordercolor`) is now supported in `plain` documents, and refined for `slides` documents, especially in PDF output.

#### Improved code diff styling

Code blocks using the `diff` language now have improved and clearer styling for added and removed lines.

### Fixed

#### Major improvements to live preview

Live preview has undergone major performance improvements and increased reliability, especially in combination with the new VS Code extension update.

Live reloading not being performed when editing subdocuments has also been fixed.

#### Fixed subdocument resolution from included files

Linking to subdocuments from files included via `.include` from a different directory now correctly resolves the subdocument path.

#### Fixed unresolved reference of local variables in body arguments

The following snippet used to cause an unresolved reference error for `y`:

```markdown
.function {a}
    x:
    .x

.function {b}
    y:
    .a
        .y

.b {hello}
```

#### Fixed paragraph spacing with floating element

Fixed an issue that caused no spacing to be present between two paragraphs if a floating element was in-between, via `.float`.

#### Fixed ToC with no level 1 headings

Table of contents are no longer empty if no level 1 headings are present, or if all are decorative.

#### Fixed line spacing in table cells

Table cells now correctly apply the same line spacing as paragraphs and lists.

---

### Sponsors

Shout out to our sponsors! 🎉

@vitto4

@serkonda7

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>