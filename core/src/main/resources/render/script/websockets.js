// Component that enables communication with a server through WebSockets.

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
    createWebSocket(serverUrl, 'reload', () => location.reload());
}

// Saves scroll position.
function saveScrollPosition() {
    history.scrollRestoration = "manual";
    const scrollPosition = {
        top: window.scrollY,
        left: window.scrollX
    };
    localStorage.setItem("scrollPosition", JSON.stringify(scrollPosition));
}

// Restores scroll position.
function restoreScrollPosition() {
    const savedPosition = localStorage.getItem("scrollPosition");
    if (savedPosition) {
        const {top, left} = JSON.parse(savedPosition);
        window.scrollTo({top, left, behavior: "auto"});
        localStorage.removeItem("scrollPosition"); // Clean up.
    }
}

// Save scroll position before reload.
window.addEventListener("beforeunload", saveScrollPosition);

// Restore after reload to keep the scroll position.
postRenderingExecutionQueue.push(restoreScrollPosition)