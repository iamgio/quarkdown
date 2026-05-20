---
name: quarkdown
description: Author Quarkdown (.qd) documents, a Markdown-superset typesetting language that compiles to HTML, PDF, or plaintext. Use when the user wants to write or edit .qd files, produce typeset output (articles, reports, slides, books, wikis, notes, static sites) beyond what plain Markdown offers, or share structured plans in a presentation-ready format. Quarkdown adds function calls (`.func {arg}`), variables, layouts, math, diagrams, and document types (plain/paged/slides/docs).
---

# Quarkdown

Quarkdown is a Markdown flavor and typesetting system. It compiles `.qd` source files to HTML, PDF, or plaintext, extending CommonMark/GFM with function calls, variables, layouts, math, custom themes, and document types. Think of it as a more readable LaTeX.

## When to use

Use this skill when the user wants to write or edit a `.qd` file, or asks for typeset output (PDF, slides, paged article, themed wiki) instead of plain Markdown. Also when they want a polished, shareable version of a plan or report.

Do **not** use it when plain Markdown is the final target (chat, READMEs, GitHub issues).

## New projects

If the user wants to start a new Quarkdown project, tell them to run `quarkdown create [dir]` themselves. It's an interactive wizard that prompts for values. Don't scaffold project files by hand.

## Locating the bundled docs

Quarkdown ships a full offline copy of its wiki and stdlib API reference. Resolve the install dir once:

```bash
QUARKDOWN_INSTALL="$(quarkdown doctor get install-dir)"
```

Resources under `$QUARKDOWN_INSTALL`:

- `docs/wiki/_nav.qd`: wiki index. Start here to find pages by topic.
- `docs/wiki/*.qd`: individual wiki pages.
- `docs/wiki/quickstart.qd`: read this first if you are new to Quarkdown.
- `docs/quarkdown-stdlib/`: stdlib API reference (function signatures, parameters, return types) as HTML.

Read the relevant wiki pages before writing anything non-trivial.

## Workflow

1. **Locate** the install dir (snippet above).
2. **Read** `docs/wiki/_nav.qd`, then `docs/wiki/quickstart.qd` if you don't know the syntax yet.
3. **Read** the specific wiki pages relevant to the task, and stdlib API pages under `docs/` when you need exact function signatures.
4. **Write** the `.qd` file.
5. **Compile to verify**: `quarkdown c <main>.qd --strict --out /tmp/quarkdown-verify`. `<main>` is usually `main.qd` but confirm against the project. `--strict` surfaces errors; `--out /tmp/quarkdown-verify` keeps build artifacts out of the user's project (otherwise output lands in `./quarkdown-output` relative to your CWD).
   - PDF export (`--pdf`) is slow because it spins up Puppeteer. Only use it when the user asks for a PDF.
   - **Never** run live preview (`-p`, `-w`) yourself; it starts a long-running web server. Suggest the command to the user instead.
6. **Report** any compile errors back to the user.
