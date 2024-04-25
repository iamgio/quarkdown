# Quarkdown

Quarkdown is a work-in-progress markup language to create documents with the minimalism and straightforwardness of Markdown,
while allowing full control over the structure and style of the document that LaTeX provides.

The base Markdown parsing is CommonMark-compliant, along with several GFM extensions.

## Overview

### Basics
CommonMark/GFM Markdown is (almost) 100% supported. Along with it, Quarkdown's flavor introduces the following:

- Math span  
  `$ \LaTeX expression $`

- Math block
  ```
  $$$
  \LaTeX expression
  $$$
  ```
  
- Image size specification:  
  - `!(150x100)[label](/url.png)` (manual width and height)
  - `!(150x_)[label](/url.png)` (manual width, automatic height, keeps the aspect ratio)
  - `!(_x100)[label](/url.png)` (automatic width, manual height, keeps the aspect ratio)


- Functions:
  - `.func`
  - `.func {arg1} {arg2}`
  - ```
    .func {arg1} {arg2}
        This is some nested body content.
    ```

### Scripting
_Block function calls_, opposite to _inline function calls_, also support a _body_ argument,
which is some nested and indented Quarkdown content. 

The following snippet is a valid Quarkdown source that calculates and displays the first 8 numbers of the Fibonacci sequence:
```markdown
.function {fib}
    .if { .islower {<<1>>} than:{2} }
        <<1>>
    .ifnot { .islower {<<1>>} than:{2} }
        .sum {
            .fib { .subtract {<<1>>} {1} }
        } {
            .fib { .subtract {<<1>>} {2} }
        }

.table
    .foreach {..8}
        .tablecolumn {$ F_<<1>> $} {.fib {<<1>>}}
```

Output:

| $F_0$ | $F_1$ | $F_2$ | $F_3$ | $F_4$ | $F_5$ | $F_6$ | $F_7$ |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|   0   |   1   |   1   |   2   |   3   |   5   |   8   |  13   |

## Goals

This is an example of what Quarkdown aims to look like:
```markdown
.docname {Quarkdown}
.docauthor {iamgio}
.aspectratio {4:3}

.tableofcontents    <-- Generates a table of contents page
                        that contains "Hello Quarkdown"
                        and "Extended Markdown"

# Hello Quarkdown

## An overview

.box {Box title}
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
  - [x] Links
  - [x] Reference links
  - [x] Autolinks (+ GFM extension)
  - [x] Images
  - [x] Reference images
  - [x] Comments
  - [x] Emphasis
  - [x] Code spans
  - [x] GFM strikethrough

- Quarkdown features
  - [x] Functions
  - [ ] Styles
  - [x] Math (LaTeX) blocks and inlines
  - [ ] Highlight
  - [ ] Subscript/superscript

- Rendering
  - [x] HTML
  - [ ] LaTeX
  
- Misc
  - GUI (with dynamic hot reload)