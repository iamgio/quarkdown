// Component that enables communication with a server through WebSockets.

function reload() {
    const iframe = document.getElementById('content-frame');
    iframe?.contentWindow?.location?.reload();
}

function startWebSockets(serverUrl) {
    startReloadWebSocket(serverUrl);
}

function createWebSocket(serverUrl, endpoint, onMessage) {
    const socket = new WebSocket(`ws://${serverUrl}/${endpoint}`);

    socket.addEventListener('open', () => {
        console.log('Connected to server ' + socket.url);
    });

    socket.addEventListener("message", onMessage);

    socket.addEventListener('close', (event) => {
        console.log(`WebSocket closed: Code=${event.code}, Reason=${event.reason}`);
    });

    socket.addEventListener("error", (error) => {
        console.error("WebSocket error:", error);
    });

    return socket;
}

// WebSocket that reloads the page when a message is received.
function startReloadWebSocket(serverUrl) {
    createWebSocket(serverUrl, 'reload', reload);
}