function initMermaid(mermaid) {
    mermaid.initialize({
        startOnLoad: false,
    });

    // Render Mermaid diagrams or load them from cache.
    postRenderingExecutionQueue.push(async () => {
        for (const element of document.querySelectorAll('.mermaid')) {
            await loadFromCacheOrRender(mermaid, element);
        }
        realignMermaidContents();
    });
}

async function loadFromCacheOrRender(mermaid, element) {
    const code = element.textContent.trim();
    const id = 'mermaid-' + hashCode(code);
    const cachedSvg = sessionStorage.getItem(id);
    element.dataset.processed = 'true';

    if (cachedSvg) {
        console.debug('Using cached SVG for diagram:', id);
        element.innerHTML = cachedSvg;
        return;
    }

    console.debug('Rendering diagram:', id);

    const diagram = await mermaid.render(id, code, element);
    const svg = diagram.svg;
    element.innerHTML = svg;
    sessionStorage.setItem(id, svg);
}

// Only after rendering the diagrams, center some misaligned elements.
function realignMermaidContents() {
    document.querySelectorAll('.mermaid foreignObject').forEach((obj) => {
        obj.style.display = 'grid';
    });
}