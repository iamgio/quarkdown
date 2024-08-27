<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-dark.svg">
    <img alt="Quarkdown banner" src="https://github.com/user-attachments/assets/68dfb3bf-9466-44f3-b220-7067322c4887">
  </picture>
  <br>
  <a href="https://www.codefactor.io/repository/github/iamgio/quarkdown"><img alt="CodeFactor" src="https://www.codefactor.io/repository/github/iamgio/quarkdown/badge/main"></a>
  <a href="https://pinterest.github.io/ktlint"><img alt="FMT: Ktlint" src="https://img.shields.io/badge/fmt-ktlint-7f52ff?logo=kotlin&logoColor=f5f5f5"></a>
  <img alt="Status: development" src="https://img.shields.io/badge/status-development-blue">
  <br>
  <br>
  <strong>Download</strong> the latest build <strong><a href="https://github.com/iamgio/quarkdown/releases">here</a></strong>&nbsp;
  <br>
  <hr>
</p>

<br>

Quarkdown is a Markdown parser and renderer that extends the capabilities of Markdown, bringing support for **functions** and many other syntax extensions.

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

## Comparison

|                       |      Markdown      |       LaTeX        |     Quarkdown      |
|-----------------------|:------------------:|:------------------:|:------------------:|
| Concise and readable  | :white_check_mark: |        :x:         | :white_check_mark: |
| Full document control |        :x:         | :white_check_mark: | :white_check_mark: |
| Scripting             |        :x:         |      Partial       | :white_check_mark: |

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

The `bin` directory contains the executable scripts. Optionally, add it to your `PATH` to access Quarkdown more easily.

Java 17 or higher is required.

## Getting started

Running the program with no command-line arguments runs it in REPL mode. This is great for familiarizing yourself with Quarkdown, but it's probably not what you're looking for.

Running `quarkdown path-to-file.qmd` will compile the given file, save the output to file and log its content.  
If the project is composed by multiple source files, the target file must be the root one, i.e. the one that includes the other files.

> [!NOTE]
> The `qmd` extension is conventionally the standard one, but any can be used.

**Options:**

- **`-o <dir>`** or **`--output <dir>`**: sets the directory of the output files. If unset, defaults to `./output`.

- **`--pretty`**: produces pretty output code. This is useful for debugging or to read the output code more easily,
  but it should be disabled in production as the results might be visually affected.

- **`--clean`**: deletes the content of the output directory before producing new files. Destructive operation.

- **`--strict`**: forces the program to exit if an error occurs. When not in strict mode, errors are shown as boxes in the document.

- **`--no-media-storage`**: disables the media storage (further information coming soon).

- **`-Dloglevel=<level>`** (JVM property): sets the log level. If set to `warning` or higher, the output content is not printed out.

## Targets

HTML is currently the only supported rendering target. LaTeX rendering is a future goal.

- **HTML**
  - :white_check_mark: Plain output (default)
  - :white_check_mark: Slides (via [reveal.js](https://revealjs.com))
  - :warning: Paged (book) (via [paged.js](https://pagedjs.org)) — currently unstable

The desired document type can be set by calling the `.doctype` function within the Markdown source itself:
- `.doctype {slides}`
- `.doctype {paged}`

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

## Status

The project is under active development.

#### Future plans

- Wiki, getting started guides and tutorials
- New themes
- Contribution guidelines
- Auto-generated stdlib documentation ([Dokka](https://github.com/Kotlin/dokka) custom plugin)
- External libraries support
- LaTeX rendering
- GUI editor / IDE plugin
