# Quarkdown

Quarkdown is a work-in-progress markup language to create documents with the minimalism and straightforwardness of Markdown,
while allowing full control over the structure and style of the document that LaTeX provides.

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

The project is under development. Currently parsed:

- Base Markdown
  - [x] Paragraphs
  - [x] Headings
  - [x] Code
  - [x] Quotes
  - [x] Lists
  - [x] Horizontal lines
  - [x] Link references
  - [ ] Tables
  - [ ] Footnotes
  - [ ] Inline content

- Quarkdown features
  - [ ] Functions
  - [x] Math (LaTeX) blocks