# server

This module contains code for Quarkdown's local webserver,
which enables automatic browser reload according to this flow:

1. The server starts;
2. When the address is opened in the browser,
the injected [`websockets.js`](../quarkdown-core/src/main/resources/render/script/websockets.js) script
begins listening to `/reload` for messages.
3. The [CLI](../quarkdown-cli), upon generating output files, sends a message via websockets to `/reload`;
4. The server forwards this message to the listening clients (browsers);
5. Each client reloads its content as soon as it receives the message.