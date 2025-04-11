function initMermaid(mermaid) {
    mermaid.initialize({
        startOnLoad: false,
    });

    // Render Mermaid diagrams.
    executionQueue.push(async () => {
        await mermaid.run();
        realignMermaidContents();
    });
}

// Only after rendering the diagrams, center some misaligned elements.
function realignMermaidContents() {
    document.querySelectorAll('.mermaid foreignObject').forEach((obj) => {
        obj.style.display = 'grid';
    });
}