# html-pdf

This module decorates the [html](../quarkdown-html) renderer to export its output to
PDF, PNG and WebP via a Chromium-family browser, driven over the Chrome DevTools Protocol.

The outcome is 1:1 with what would be displayed in a Chrome browser.

## Options

- `--render html --pdf` or `--render html-pdf` for PDF rendering.
- `--render html-png` or `--render html-webp` for image rendering.

`-r` is short for `--render`.

Note that Quarkdown's CLI uses `--render html` by default, hence PDF rendering can be achieved via just `--pdf`.

For requirements and additional options, see the [PDF export](https://quarkdown.com/wiki/pdf-export) page.
