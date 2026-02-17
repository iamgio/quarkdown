# Changelog

## [Unreleased]

### Added

#### Docs

TODO also mention page margins
TODO begin via `quarkdown create`

#### New themes: Galactic (color) and Hyperlegible (layout)

TODO img

#### GitHub-style alerts

GitHub's alert syntax is now supported, making it easier to migrate from other tools:

```markdown
> [!NOTE]
> This is a note
```

Note that Quarkdown's original syntax is still supported *and recommended*, especially for English documents:

```markdown
> Note: This is a note
```

#### Subdocument links now allow anchors

Links to Quarkdown subdocuments now support anchors, to link to specific sections:

```markdown
[Page](page.qd#section)
```

#### Customizable page numbering format

The `.formatpagenumber {format}` function overrides the page numbering format from the current page onward. It accepts the same format specifiers as `.numbering`, and applies to both page counters and table of contents.

```markdown
.pagemargin {topcenter}
    .currentpage

# First page

.formatpagenumber {i}

# Second page

# Third page
```

TODO img from wiki

Thanks @OverSamu!

#### Horizontal/vertical gap customization of `.grid`

The `.grid` function now accepts `hgap` and `vgap` parameters to customize the horizontal and vertical gaps between grid items. `gap` still works as a shorthand for both.

Thanks @OverSamu!

#### Icons

TODO `.icon` relying on [Bootstrap Icons](https://icons.getbootstrap.com/#icons)

#### New output target: plain text

Plain text is Quarkdown's second output target: `--render plaintext`

This has no particular use case. It was needed to implement docs' search feature in the first place.

### Changed

#### `.css` doesn't require `!important` anymore

The `.css` function now injects `!important` automatically at the end of each rule.

#### Revised navigation sidebar

The navigation sidebar, visible in `plain` and `paged` documents on web view, is now easier to navigate, with all entries visible at once,
and more accessible for screen readers.

TODO img from mock

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

### Sponsors

Shout out to our sponsors! 🎉

@vitto4

@serkonda7

<a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>