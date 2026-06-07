# server

This module contains code for Quarkdown's local webserver,
which serves static files and drives live preview.

Endpoints:
- `/` for static files, relative to the origin directory;
- `/live/[file]` for wrapping HTML files with live preview capabilities;
- `/reload` for live reloading: `GET` opens a Server-Sent Events stream that subscribers (browser clients) listen on,
  while `POST` broadcasts a reload event to every active subscriber (used by Quarkdown CLI after each compile).

For architectural details, see [Inside Quarkdown - How does live preview work?](https://quarkdown.com/wiki/inside-live-preview).
