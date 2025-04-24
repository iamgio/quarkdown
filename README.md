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

Combined with live preview and :zap: fast compilation speed, Quarkdown simply gets the work done.

&nbsp;

<p align="center">
<img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/video/livepreview.gif" alt="Live preview" />
</p>

&nbsp;

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

### As simple as you expect...

<p align="center">
  <img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/code-paper.png" alt="Paper code demo">
  <p align="center"><em>Inspired by: <a href="https://news.mit.edu/2025/x-ray-flashes-nearby-supermassive-black-hole-accelerate-mysteriously-0113">X-ray flashes from a nearby supermassive black hole accelerate mysteriously</a></em></p>
</p>

### ...as complex as you need.

<p align="center">
  <img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/code-chart.png" alt="Chart code demo">
</p>

## Targets

- **HTML**
  - :white_check_mark: Plain output (default)
  - :white_check_mark: Slides <sup>via [reveal.js](https://revealjs.com)</sup>
  - :white_check_mark: Paged (books, articles) <sup>via [paged.js](https://pagedjs.org)</sup>  
    *Paged documents require a webserver to render in the browser. See the [`-p`](#options) option below.*

- **PDF**
  - :white_check_mark: All document types and features supported by HTML are also supported when exporting to PDF.
  - Check the wiki's [PDF export](https://github.com/iamgio/quarkdown/wiki/pdf-export) page to learn more.

The desired document type can be set by calling the [`.doctype` function](https://github.com/iamgio/quarkdown/wiki/document-metadata) within the source itself:
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
| Targets               |     HTML, PDF      |        HTML        |        PDF         |  HTML, PDF, ePub   |        HTML        |

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

&nbsp;

## Installation

Download `quarkdown.zip` from the [releases](https://github.com/iamgio/quarkdown/releases) page or build it yourself with `gradlew distZip`, and unzip it.    
If you'd rather keep it minimal, `gradlew build` produces only the JAR file.

- The `bin` directory contains the executable scripts. Optionally, add it to your `PATH` to access Quarkdown more easily.
- The `lib/qmd` directory contains `.qmd` libraries that can be imported into a project.

Java 17 or higher is required.

## Getting started

### Creating a project

Running **`quarkdown create [directory]`** will launch the prompt-based project wizard, making it quicker than ever
to set up a new Quarkdown project, with all [metadata](https://github.com/iamgio/quarkdown/wiki/document-metadata) and initial content already present.

For more information about the project creator, check out its [wiki page](https://github.com/iamgio/quarkdown/wiki/cli%3A-project-creator).

Alternatively, you may manually create a `.qmd` source file and start from there.

### Compiling

Running **`quarkdown c file.qmd`** will compile the given file and save the output to file.

> If the project is composed by multiple source files, the target file must be the root one, i.e. the one that includes the other files.
>
> - [How to include other files?](https://github.com/iamgio/quarkdown/wiki/including-other-quarkdown-files)

If you would like to familiarize yourself with Quarkdown instead, `quarkdown repl` lets you play with an interactive REPL mode.

#### Options

- **`-o <dir>`** or **`--output <dir>`**: sets the directory of the output files. If unset, defaults to `./output`.

- **`-p`** or **`--preview`**: enables automatic content reloading after compiling.  
  If a [webserver](https://github.com/iamgio/quarkdown/wiki/cli%3A-webserver) is not running yet, it is started and the document is opened in the default browser.  
  This is required in order to render paged documents in the browser.

- **`-w`** or **`--watch`**: recompiles the source everytime a file from the source directory is changed.  
  
> [!TIP]
> Combine `-p -w` to achieve ***live preview***!

- **`--pdf`**: produces a PDF file. Learn more in the wiki's [*PDF export*](https://github.com/iamgio/quarkdown/wiki/pdf-export) page.

- `--server-port <port>`: optional customization of the local webserver's port. Defaults to `8089`.

- `-l <dir>` or `--libs <dir>`: sets the directory where external libraries can be loaded from. If unset, defaults to `<install dir>/lib/qmd`. [(?)](https://github.com/iamgio/quarkdown/wiki/importing-external-libraries)

- `--pretty`: produces pretty output code. This is useful for debugging or to read the output code more easily,
  but it should be disabled in production as the results might be visually affected.

- `--clean`: deletes the content of the output directory before producing new files. Destructive operation.

- `--strict`: forces the program to exit if an error occurs. When not in strict mode, errors are shown as boxes in the document.

- `--no-media-storage`: turns the media storage system off. [(?)](https://github.com/iamgio/quarkdown/wiki/media-storage)

- `-Dloglevel=<level>` (JVM property): sets the log level. If set to `warning` or higher, the output content is not printed out.

### Mock document

&nbsp;

<p align="center">
  <img width="550" src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/mock-demo.png" alt="Mock document demo">
</p>

***Mock***, written in Quarkdown, is a comprehensive collection of visual elements offered by the language,
making it ideal for exploring and understanding its key features — all while playing and experimenting hands-on with a concrete outcome in the form of pages or slides.

- The document's source files are available in the [`mock`](mock) directory, and can be compiled via `quarkdown c mock/main.qmd -p`.
- The PDF artifacts generated for all possible theme combinations are available and can be viewed in the [`generated`](https://github.com/iamgio/quarkdown/tree/generated/pdf/mock) branch.  

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