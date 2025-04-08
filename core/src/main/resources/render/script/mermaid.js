function initMermaid(mermaid) {
    notifyTaskStarted();
    mermaid.initialize({
        startOnLoad: false,
    });

    // Render Mermaid diagrams.
    executionQueue.push(async () => {
        await mermaid.run();
        notifyTaskFinished();
    });
}