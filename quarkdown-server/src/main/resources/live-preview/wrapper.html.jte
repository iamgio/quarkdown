@param String srcFile
@param String serverHost
@param String serverPort

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quarkdown Live Preview</title>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            height: 100%;
            width: 100%;
            overflow: hidden;
            background-color: white;
        }

        .frame-container {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            overflow: hidden;
            background-color: white;
            transform: translateZ(0);
        }

        .content-frame {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border: none;
            margin: 0;
            padding: 0;
            overflow: hidden;
            z-index: 999999;
            background-color: white;
            will-change: opacity, transform;
            transform: translateZ(0);
            backface-visibility: hidden;
            -webkit-backface-visibility: hidden;
            -webkit-transform-style: preserve-3d;
            transform-style: preserve-3d;
        }

        .hidden {
            opacity: 0;
            z-index: 999998;
            pointer-events: none;
            transition: opacity 0.3s ease-in;
        }

        .visible {
            opacity: 1;
            z-index: 999999;
            pointer-events: auto;
            transition: opacity 0.3s ease-out;
        }

        .debug .frame-container {
            display: flex;
            flex-direction: row;
        }

        .debug .content-frame {
            position: relative;
            width: 50%;
            border: 2px solid red;
        }

        .debug .hidden {
            opacity: .5;
        }
    </style>
</head>
<body>
<!-- Double frame buffer makes preview updates smoother -->
<div class="frame-container">
    <iframe id="frame-0" class="content-frame visible" src="${srcFile}"></iframe>
    <iframe id="frame-1" class="content-frame hidden" src="${srcFile}"></iframe>
</div>
<script data-name="websockets">
    /**
     * Starts a WebSocket connection to the specified server and endpoint.
     * @param {string} serverUrl - The server URL (e.g., `localhost:8080`).
     * @param {string} endpoint - The WebSocket endpoint (e.g., `reload`).
     * @param {function} onMessage - The callback function to handle incoming messages.
     * @returns {WebSocket} The WebSocket instance.
     */
    function startWebSocket(serverUrl, endpoint, onMessage) {
        const socket = new WebSocket(`ws://${'$'}{serverUrl}/${'$'}{endpoint}`);

        socket.addEventListener('open', () => {
            console.log('Connected to server ' + socket.url);
        });

        socket.addEventListener("message", onMessage);

        socket.addEventListener('close', (event) => {
            console.log(`WebSocket closed: Code=${'$'}{event.code}, Reason=${'$'}{event.reason}`);
            // Automatically reconnect after a brief delay.
            setTimeout(() => startWebSocket(serverUrl, endpoint, onMessage), 1000);
        });

        socket.addEventListener("error", (error) => {
            console.error("WebSocket error:", error);
        });

        return socket;
    }

    /**
     * Starts the WebSocket that reload the content when a message is received.
     */
    function startReloadWebSocket(serverUrl) {
        startWebSocket(serverUrl, 'reload', reload);
    }
</script>
<script data-name="messaging">
    // Scalable message listener registry, with event type as the key and the handler function as the value.
    const messageListeners = new Map();

    // Listens for messages from the iframe content.
    window.addEventListener('message', (event) => {
        const data = event.data;
        if (!data || data.source !== 'quarkdown') return;

        const listener = messageListeners.get(data.event);
        listener?.(data);
    });
</script>
<script data-name="double-buffering">
    const NUM_FRAMES = 2;

    let activeFrameId = 0;
    let inactiveFrameId = 1;

    // Stored scroll position, continuously tracked on the active frame.
    let scrollX = 0;
    let scrollY = 0;
    let isScrollStickyToBottom = false;

    // During a reload, the swap is deferred until the hidden frame is fully ready:
    // both loaded (onload) and rendered (postRenderingCompleted).
    // Their relative ordering is unpredictable (postMessage vs onload are separate tasks),
    // so the swap commits as soon as both signals have arrived.
    let reloadPending = false;
    let frameLoaded = false;
    let frameRendered = false;
    let swapFallbackTimer = null;

    /**
     * Suppresses CSS smooth scrolling inside the given frame, so that
     * programmatic scroll restoration is always instant.
     * @param {HTMLIFrameElement} frame - The frame element
     */
    function suppressSmoothScroll(frame) {
        const doc = frame.contentDocument;
        if (doc) doc.documentElement.style.scrollBehavior = 'auto';
    }

    /**
     * Disables the browser's automatic scroll restoration on the given frame,
     * so that our manual scroll restoration during frame swaps is not overridden
     * by the browser's async restoration after `location.reload()`.
     * @param {HTMLIFrameElement} frame - The frame element
     */
    function disableAutoScrollRestoration(frame) {
        const history = frame.contentWindow?.history;
        if (history) history.scrollRestoration = 'manual';
    }

    // Re-apply after every iframe reload, since the document is replaced.
    for (let i = 0; i < NUM_FRAMES; i++) {
        document.getElementById(`frame-${'$'}{i}`).addEventListener('load', function () {
            suppressSmoothScroll(this);
            disableAutoScrollRestoration(this);
        });
    }

    /**
     * @returns {HTMLIFrameElement} The frame element which is currently active (visible).
     */
    function getActiveFrame() {
        return document.getElementById(`frame-${'$'}{activeFrameId}`);
    }

    /**
     * @returns {HTMLIFrameElement} The frame element which is currently inactive (hidden).
     */
    function getInactiveFrame() {
        return document.getElementById(`frame-${'$'}{inactiveFrameId}`);
    }

    /**
     * @returns {boolean} Whether the scroll position is at the very bottom of the page
     */
    function isScrolledToBottom(frame) {
        const win = frame.contentWindow;
        if (!win) return false;

        const scrollableHeight = win.document.body.scrollHeight;
        const viewportHeight = win.innerHeight;
        const currentY = win.scrollY || 0;

        // If content fits in viewport or scrolled to bottom.
        return scrollableHeight <= viewportHeight || currentY >= scrollableHeight - viewportHeight - 1;
    }

    /**
     * Reads the current scroll position from the given frame into the stored values.
     * @param {HTMLIFrameElement} frame - The frame to read from
     */
    function captureScrollPosition(frame) {
        const win = frame.contentWindow;
        if (!win) return;
        scrollX = win.scrollX || 0;
        scrollY = win.scrollY || 0;
        isScrollStickyToBottom = isScrolledToBottom(frame);
    }

    /**
     * Restores the stored scroll position on the given frame.
     * If the user was at the bottom, keeps them at the bottom.
     * @param {HTMLIFrameElement} frame - The frame to scroll
     */
    function restoreScrollPosition(frame) {
        suppressSmoothScroll(frame);
        const win = frame.contentWindow;
        if (!win) return;
        const y = isScrollStickyToBottom ? win.document.body.scrollHeight : scrollY;
        win.scrollTo(scrollX, y);
    }

    /**
     * Starts continuously tracking scroll on the given frame,
     * storing the position for later restoration.
     * @param {HTMLIFrameElement} frame - The frame to track
     */
    function setupScrollTracking(frame) {
        const win = frame.contentWindow;
        if (!win) return;

        win.onscroll = () => {
            scrollX = win.scrollX || 0;
            scrollY = win.scrollY || 0;
            isScrollStickyToBottom = isScrolledToBottom(frame);
        };
    }

    /**
     * Stops scroll tracking on the given frame.
     * @param {HTMLIFrameElement} frame
     */
    function teardownScrollTracking(frame) {
        const win = frame.contentWindow;
        if (win) win.onscroll = null;
    }

    /**
     * Toggles the visibility class of a frame.
     * @param {HTMLElement} frame - The frame element
     * @param {boolean} isVisible - Whether the frame should be visible
     */
    function setFrameVisibility(frame, isVisible) {
        if (isVisible) {
            frame.classList.remove('hidden');
            frame.classList.add('visible');
        } else {
            frame.classList.remove('visible');
            frame.classList.add('hidden');
        }
    }

    /**
     * Attempts to commit the swap if both readiness signals have arrived.
     * Does nothing if either the frame hasn't loaded or hasn't rendered yet.
     */
    function maybeCommitSwap() {
        if (frameLoaded && frameRendered) {
            commitSwap();
        }
    }

    /**
     * Syncs the root background color to match the active frame's content,
     * so that any opacity gap during the crossfade doesn't flash a mismatched color.
     * @param {HTMLIFrameElement} frame - The frame to sample from
     */
    function syncBackgroundColor(frame) {
        const body = frame.contentDocument?.body;
        if (!body) return;
        const bg = frame.contentWindow?.getComputedStyle(body)?.backgroundColor;
        if (bg) {
            document.querySelector('.frame-container').style.backgroundColor = bg;
            document.documentElement.style.backgroundColor = bg;
        }
    }

    /**
     * Performs the frame swap. Only called when the hidden frame is fully ready
     * (loaded and rendered), so scroll can be restored accurately on the
     * still-hidden frame before it becomes visible.
     */
    function commitSwap() {
        if (!reloadPending) return;
        clearTimeout(swapFallbackTimer);

        const outgoing = getActiveFrame();
        const incoming = getInactiveFrame();

        // Match the root background to the incoming frame's content
        // to prevent flashing during the crossfade.
        syncBackgroundColor(incoming);

        // Snapshot the scroll position from the outgoing frame at the last possible moment,
        // so any user scrolling since the reload started is captured.
        captureScrollPosition(outgoing);

        // Detach all handlers.
        teardownScrollTracking(outgoing);
        teardownScrollTracking(incoming);
        incoming.onload = null;

        // Restore scroll on the still-hidden incoming frame.
        restoreScrollPosition(incoming);

        // Swap visibility.
        setFrameVisibility(incoming, true);
        setFrameVisibility(outgoing, false);

        // Update IDs.
        const prev = activeFrameId;
        activeFrameId = inactiveFrameId;
        inactiveFrameId = prev;

        // Start tracking on the new active frame.
        setupScrollTracking(incoming);

        // Reset reload state.
        reloadPending = false;
        frameLoaded = false;
        frameRendered = false;
    }
</script>
<script data-name="reload">
    /**
     * Checks whether two URLs differ in a way that causes the browser to trigger a page load
     * when assigning one to an iframe's `src`. Hash-only differences do not trigger a load (#419).
     */
    function differsIgnoringHash(url1, url2) {
        try {
            const u1 = new URL(url1);
            const u2 = new URL(url2);
            return !(u1.origin === u2.origin &&
                u1.pathname === u2.pathname &&
                u1.search === u2.search);
        } catch (e) {
            return false;
        }
    }

    /**
     * Reloads the preview using double buffering to prevent flickering.
     */
    function reload() {
        reloadPending = true;
        frameLoaded = false;
        frameRendered = false;
        clearTimeout(swapFallbackTimer);

        inactiveFrameId = (activeFrameId + 1) % NUM_FRAMES;
        const active = getActiveFrame();
        const nextFrame = getInactiveFrame();

        // Make sure the frame to reload is properly hidden.
        setFrameVisibility(nextFrame, false);

        // (#199) If the URL has changed beyond just the hash (e.g. different slide, subdocument, etc.),
        // sync the inactive frame's src. Setting src already triggers a browser load.
        const activeUrl = active.contentWindow?.location?.href;
        if (activeUrl && differsIgnoringHash(nextFrame.src, activeUrl)) {
            nextFrame.src = activeUrl;
            nextFrame.onload = onFrameLoaded;
            return;
        }

        // For same-page navigations (including hash-only changes), sync the hash and reload manually.
        if (activeUrl && nextFrame.src !== activeUrl) {
            nextFrame.src = activeUrl;
        }

        nextFrame.onload = onFrameLoaded;
        nextFrame?.contentWindow?.location?.reload();
    }

    /**
     * Signal: the hidden frame's document and subresources have loaded.
     */
    function onFrameLoaded() {
        frameLoaded = true;
        // Start a fallback timer for content that never sends postRenderingCompleted.
        // This must be long enough for the content's rendering pipeline to complete,
        // especially for paged documents where paged.js layout can take several seconds.
        swapFallbackTimer = setTimeout(commitSwap, 5000);
        maybeCommitSwap();
    }

    /**
     * Signal: the hidden frame's content has finished its async rendering pipeline.
     * For Quarkdown content this fires after the post-rendering execution queue completes.
     */
    function onFrameRendered() {
        if (!reloadPending) {
            // Initial page load (no reload in progress).
            const active = getActiveFrame();
            syncBackgroundColor(active);
            setupScrollTracking(active);
            return;
        }
        frameRendered = true;
        maybeCommitSwap();
    }

    messageListeners.set('postRenderingCompleted', onFrameRendered);
</script>
<script>startReloadWebSocket("${serverHost}:${serverPort}")</script>
</body>
</html>
