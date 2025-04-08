function initMermaid(mermaid) {
    mermaid.initialize({
        startOnLoad: false,
        securityLevel: 'sandbox',
    });

    // Render Mermaid diagrams inside <pre class="mermaid"> tags.
    executionQueue.push(() => {
        const diagrams = document.querySelectorAll('pre.mermaid');
        mermaid.run({
            nodes: diagrams,
        })
    });
}