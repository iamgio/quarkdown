# html

This module provides an extension to render Quarkdown ASTs to:
- HTML, CSS and JavaScript;
- PDF (via [Puppeteer](https://pptr.dev)).

## Options

- `--render html` for HTML rendering
- `--render html --pdf` or `--render html-pdf` for PDF rendering.

`-r` is short for `--render`.

Note that Quarkdown's CLI uses `--render html` by default, hence PDF rendering can be achieved via just `--pdf`.