# Quarkdown

Quarkdown is a work-in-progress markup language to create documents with the minimalism and straightforwardness of Markdown,
while allowing full control over the structure and style of the document that LaTeX provides.

The base Markdown parsing is CommonMark-compliant, along with several GFM extensions.

## Overview

This is an example of what Quarkdown aims to look like:
```markdown
.docname [Quarkdown]
.docauthor [iamgio]
.aspectratio [4:3]

.tableofcontents    <-- Generates a table of contents page
                        that contains "Hello Quarkdown"
                        and "Extended Markdown"

# Hello Quarkdown

## An overview

.box [Box title]
    This is some text within a box.
    1. And this
    2. is a
    3. Markdown list

.center
    This content is **centered**!
    ![An image](img.png)

# Extended Markdown

Quarkdown's custom Markdown flavor introduces new blocks as well!

$ This is a LaTeX expression $

```

## Status

The project is under development. Currently supported:

- Base Markdown blocks
  - [x] Paragraphs
  - [x] Headings
  - [x] Code
  - [x] Quotes
  - [x] Lists (+ GFM tasks)
  - [x] Horizontal lines
  - [x] Link references
  - [x] GFM tables
  - [ ] GFM footnotes

- Base Markdown inline
  - [x] Text
  - [x] Link
  - [x] Reference link
  - [x] Autolink (+ GFM extension)
  - [x] Image
  - [x] Reference image
  - [x] Comment
  - [x] Emphasis
  - [x] Code span
  - [x] GFM strikethrough

- Quarkdown features
  - [ ] Functions
  - [ ] Styles
  - [x] Math (LaTeX) blocks
  - [ ] Highlight
  - [ ] Subscript/superscript

- Rendering
  - [x] HTML
  - [ ] LaTeX (in the future...)