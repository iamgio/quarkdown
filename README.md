<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://github.com/user-attachments/assets/8f705eb8-8405-4e02-8e84-50eaaba7f5df">
    <source media="(prefers-color-scheme: light)" srcset="https://github.com/user-attachments/assets/68dfb3bf-9466-44f3-b220-7067322c4887">
    <img alt="Quarkdown banner" src="https://github.com/user-attachments/assets/68dfb3bf-9466-44f3-b220-7067322c4887">
  </picture>
  <br>
  <a href="https://www.codefactor.io/repository/github/iamgio/quarkdown"><img alt="CodeFactor" src="https://www.codefactor.io/repository/github/iamgio/quarkdown/badge/main"></a>
  <img alt="Status: development" src="https://img.shields.io/badge/status-development-blue">
  <br>&nbsp;
  <hr>
</p>

Quarkdown is a Markdown parser and renderer that extends the capabilities of CommonMark and GFM with its own *Quarkdown
flavor*.

The standout feature the flavor introduces is its support for  **functions**, allowing users to access an
extensive [standard library](stdlib/src/main/kotlin/eu/iamgio/quarkdown/stdlib), use conditional statements and loops,
define new functions and variables — all within Markdown.

This out-of-the-box scripting support opens doors to complex and dynamic content that would be otherwise impossible
to achieve with vanilla Markdown.

&nbsp;

---

<h2 align="center">Check out the demo presentation <a href="https://iamgio.eu/quarkdown/demo" target="_blank">here</a></h3>
<p align="center">
Built with Quarkdown itself — <a href="demo/demo.qmd" target="_blank"><strong>source code</strong></a>
</p>

---

&nbsp;

## Comparison

|                       |      Markdown      |       LaTeX        |     Quarkdown      |
|-----------------------|:------------------:|:------------------:|:------------------:|
| Concise and readable  | :white_check_mark: |        :x:         | :white_check_mark: |
| Full document control |        :x:         | :white_check_mark: | :white_check_mark: |
| Scripting             |        :x:         |        :x:         | :white_check_mark: |

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
\section{Section}
\subsection{Subsection}
\begin{enumerate}
    \item \textbf{First} item
    \item \textbf{Second} item
\end{itemize}
\begin{center}
    This text is \textit{centered}.
\end{center}
```

</td>
<td>

```markdown
# Section

## Subsection

1. **First** item
2. **Second** item

.center
    This text is _centered_.
```

</td>
</tr>
</tbody>
</table>

## Targets

HTML is currently the only supported rendering target. LaTeX rendering is a future goal.

- **HTML**
  - :white_check_mark: Plain output (default)
  - :white_check_mark: Slides (via [reveal.js](https://revealjs.com))
  - :warning: Paged (book) (via [paged.js](https://pagedjs.org)) — currently unstable

The desired document type can be set by calling the `.doctype` function within the Markdown source itself:
- `.doctype {slides}`
- `.doctype {paged}`

> [!NOTE]
> Make sure to set the output directory in order to save the output to file.    
> This can be done by setting the command line argument `--out <dir>` or `-o <dir>`. 

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
