<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-dark.svg">
    <img alt="Quarkdown banner" src="https://github.com/user-attachments/assets/68dfb3bf-9466-44f3-b220-7067322c4887">
  </picture>
  <br>
  <a href="https://github.com/iamgio/quarkdown/wiki"><img alt="Wiki" src="https://img.shields.io/badge/wiki-read-darkcyan"></a>
  <a href="https://pinterest.github.io/ktlint"><img alt="FMT: Ktlint" src="https://img.shields.io/badge/fmt-ktlint-7f52ff?logo=kotlin&logoColor=f5f5f5"></a>
  <a href="https://www.codefactor.io/repository/github/iamgio/quarkdown"><img alt="CodeFactor" src="https://www.codefactor.io/repository/github/iamgio/quarkdown/badge/main"></a>
  <br>
  <br>
  <strong>Download</strong> the latest build <strong><a href="https://github.com/iamgio/quarkdown/releases">here</a></strong>&nbsp;
  <br>
  <hr>
</p>

&nbsp;

Quarkdown is a modern Markdown-based typetting system, designed around the key concept of **versatility**, by seamlessly compiling a project
into a print-ready book or an interactive presentation.
All through an incredibly powerful Turing-complete extension of Markdown, ensuring your ideas flow automatically into paper.

&nbsp;

<p align="center">
  <img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/paged-demo.png" alt="Paper demo">
  <p align="center"><em>Original credits: <a href="https://arxiv.org/abs/1706.03762v7">Attention Is All You Need</a></em></p>
</p>

<br>

Born as an extension of CommonMark and GFM, the Quarkdown Flavor brings **functions** to Markdown, along with many other syntax extensions.

<br>

> This is a function call:
> ```
> .somefunction {arg1} {arg2}
>     Body argument
> ```

<br>

**Possibilities are unlimited** thanks to an ever-expanding [standard library](stdlib/src/main/kotlin/eu/iamgio/quarkdown/stdlib),
which offers layout builders, I/O, math, conditional statements and loops.

**Not enough?** You can still define your own functions and variables — all within Markdown.
You can even create awesome libraries for everyone to use.

<br>

> ```
> .function {greet}
>     to from:
>     **Hello, .to** from .from!
>
> .greet {world} from:{iamgio}
> ```
> Result: **Hello, world** from iamgio!

<br>

This out-of-the-box scripting support opens doors to complex and dynamic content that would be otherwise impossible
to achieve with vanilla Markdown.

Check the [wiki](https://github.com/iamgio/quarkdown/wiki) to learn more about the language and its features.

&nbsp;

---

<h2 align="center">Check out the demo presentation <a href="https://iamgio.eu/quarkdown/demo" target="_blank">here</a></h3>
<p align="center">
Built with Quarkdown itself — <a href="demo/demo.qmd" target="_blank"><strong>source code</strong></a>
<br><br>
<em>(Desktop view is suggested)</em>
</p>

---

&nbsp;

## Targets

- **HTML**
  - :white_check_mark: Plain output (default)
  - :white_check_mark: Slides (via [reveal.js](https://revealjs.com))
  - :white_check_mark: Paged (books, articles) (via [paged.js](https://pagedjs.org)) - *Requires a webserver. See [Server](#server) below.*
  - Quarkdown's HTML is PDF-ready: check the [wiki](https://github.com/iamgio/quarkdown/wiki/pdf-export)
    to learn how to convert an artifact to PDF.

The desired document type can be set by calling the [`.doctype` function](https://github.com/iamgio/quarkdown/wiki/document-metadata) within the Markdown source itself:
- `.doctype {slides}`
- `.doctype {paged}`

## Comparison

|                       |     Quarkdown      |      Markdown      |       LaTeX        |      AsciiDoc      |        MDX         |
|-----------------------|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|
| Concise and readable  | :white_check_mark: | :white_check_mark: |        :x:         | :white_check_mark: | :white_check_mark: |
| Full document control | :white_check_mark: |        :x:         | :white_check_mark: |        :x:         |        :x:         |
| Scripting             | :white_check_mark: |        :x:         |      Partial       |        :x:         | :white_check_mark: |
| Book/article export   | :white_check_mark: |        :x:         | :white_check_mark: | :white_check_mark: |    Third-party     |
| Presentation export   | :white_check_mark: |        :x:         | :white_check_mark: | :white_check_mark: |    Third-party     |

<table>
  <thead>
    <tr>
      <th>LaTeX</th>
      <th>Quarkdown</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>

```latex
\tableofcontents

\section{Section}

\subsection{Subsection}

\begin{enumerate}
    \item \textbf{First} item
    \item \textbf{Second} item
\end{itemize}

\begin{center}
    This text is \textit{centered}.
\end{center}

\begin{figure}[!h]
    \centering
    \begin{subfigure}[b]
        \includegraphics[width=0.3\linewidth]{img1.png}
    \end{subfigure}
    \begin{subfigure}[b]
        \includegraphics[width=0.3\linewidth]{img2.png}
    \end{subfigure}
    \begin{subfigure}[b]
        \includegraphics[width=0.3\linewidth]{img3.png}
    \end{subfigure}
\end{figure}
```

</td>
<td>

```markdown
.tableofcontents

# Section

## Subsection

1. **First** item
2. **Second** item

.center
    This text is _centered_.

.row alignment:{spacebetween}
    ![Image 1](img1.png)

    ![Image 2](img2.png)
    
    ![Image 3](img3.png)
```

</td>
</tr>
</tbody>
</table>

## Installation

Download `quarkdown.zip` from the [releases](https://github.com/iamgio/quarkdown/releases) page or build it yourself with `gradlew distZip`, and unzip it.    
If you'd rather keep it minimal, `gradlew build` produces only the JAR file.

- The `bin` directory contains the executable scripts. Optionally, add it to your `PATH` to access Quarkdown more easily.
- The `lib/qmd` directory contains `.qmd` libraries that can be imported into a project.

Java 17 or higher is required.

## Getting started

Running `quarkdown c file.qmd` will compile the given file and save the output to file.

> If the project is composed by multiple source files, the target file must be the root one, i.e. the one that includes the other files.
>
> - [How to include other files?](https://github.com/iamgio/quarkdown/wiki/including-other-quarkdown-files)

If you would like to familiarize yourself with Quarkdown instead, `quarkdown repl` lets you play with an interactive REPL mode.

### Options

- **`-o <dir>`** or **`--output <dir>`**: sets the directory of the output files. If unset, defaults to `./output`.

- **`-l <dir>`** or **`--libs <dir>`**: sets the directory where external libraries can be loaded from. If unset, defaults to `<install dir>/lib/qmd`. [(?)](https://github.com/iamgio/quarkdown/wiki/importing-external-libraries)

- **`-s`** or **`--use-server`**: inject additional code to communicate to the webserver, in order to reload the browser automatically after compiling;

- **`--server-port <port>`**: optional customization of the webserver's port. Defaults to `8089`.

- **`--pretty`**: produces pretty output code. This is useful for debugging or to read the output code more easily,
  but it should be disabled in production as the results might be visually affected.

- **`--clean`**: deletes the content of the output directory before producing new files. Destructive operation.

- **`--strict`**: forces the program to exit if an error occurs. When not in strict mode, errors are shown as boxes in the document.

- **`--no-media-storage`**: turns the media storage system off. [(?)](https://github.com/iamgio/quarkdown/wiki/media-storage)

- **`-Dloglevel=<level>`** (JVM property): sets the log level. If set to `warning` or higher, the output content is not printed out.

### Server

Quarkdown's webserver allows direct communication between the compiler and the browser,
enabling automatic content reloading. Live reloading will also be available in the near future.

> [!IMPORTANT]
> A webserver is **mandatory** in order to show *paged* documents, because of a paged.js requirement.  
> For that purpose, you can also use other servers, such as Visual Studio Code's *Live Preview*, if you prefer.

The server can be started via `quarkdown start`, with the following options:

- **`-f <file>`** or **`--file <file>`**: (*mandatory*) the file the server should point to. It would preferably be the output directory of the compilation.

- **`-p <port>`** or **`--port <port>`**: the webserver's port. If unset, defaults to `8089`.

- **`-o`** or **`--open`**: if set, opens the target file in the default browser.

## Themes

Quarkdown comes with a set of themes that can give a unique look to your document.

- [How to apply a theme?](https://github.com/iamgio/quarkdown/wiki/themes)

> [Theme contributions](core/src/main/resources/render/theme) are welcome!  
> Please make sure they work well with all the three document types before submitting.

## Scripting

<details>
<summary><strong>Iterative Fibonacci</strong></summary>

```
.var {t1} {0}
.var {t2} {1}

.table
    .foreach {0..8}
        n:
        | $ F_{.n} $ |
        |:----------:|
        |    .t1     |
        .var {tmp} {.sum {.t1} {.t2}}
        .var {t1} {.t2}
        .var {t2} {.tmp}
```

| $F_0$ | $F_1$ | $F_2$ | $F_3$ | $F_4$ | $F_5$ | $F_6$ | $F_7$ | $F_8$ |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|   0   |   1   |   1   |   2   |   3   |   5   |   8   |  13   |  21   |

</details>

<details>
<summary><strong>Recursive Fibonacci</strong></summary>

> The recursive approach is significantly slower than the iterative one.

```
.function {fib}
    n:
    .if { .islower {.n} than:{2} }
        .n
    .ifnot { .islower {.n} than:{2} }
        .sum {
            .fib { .subtract {.n} {1} }
        } {
            .fib { .subtract {.n} {2} }
        }
  
.table
    .foreach {0..8}
        | $ F_{.1} $ |
        |:----------:|
        | .fib {.1}  |
```

| $F_0$ | $F_1$ | $F_2$ | $F_3$ | $F_4$ | $F_5$ | $F_6$ | $F_7$ | $F_8$ |
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|   0   |   1   |   1   |   2   |   3   |   5   |   8   |  13   |  21   |

</details>


## Concept

The logo resembles the original [Markdown icon](https://github.com/dcurtis/markdown-mark), with focus on Quarkdown's completeness,
richness of features and customization options, emphasized by the revolving arrow all around the sphere.

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-light.svg">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-dark.svg">
    <img alt="Quarkdown icon" src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-dark.svg">
  </picture>
</p>

What could be mistaken for a planet is actually a **quark** or, more specifically, a **down quark**,
an elementary particle that is a major constituent of matter: they give life to every complex structure we know of,
while also being one of the lightest objects in existence.

This is, indeed, the concept **Quarkdown** is built upon. 