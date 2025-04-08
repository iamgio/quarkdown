function initMermaid(mermaid) {
    notifyTaskStarted();
    mermaid.initialize({
        startOnLoad: false,
        securityLevel: 'sandbox',
    });

    // Render Mermaid diagrams.
    executionQueue.push(async () => {
        await mermaid.run();
        notifyTaskFinished();
    });
}