# server

This module contains code for Quarkdown's local webserver,
which serves static files and allows for WebSockets-based live preview.

Endpoints:
- `/` for static files, relative to the origin directory;
- `/live/[file]` for wrapping HTML files with live preview capabilities;
- `/reload` for WebSocket connections to notify clients of file changes.

For architectural details, see [Inside Quarkdown - How does live preview work?](https://quarkdown.com/wiki/inside-live-preview).