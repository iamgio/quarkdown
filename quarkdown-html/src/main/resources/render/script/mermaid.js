function initMermaid(mermaid) {
    mermaid.initialize({
        startOnLoad: false,
    });

    // Render Mermaid diagrams or load them from cache.
    preRenderingExecutionQueue.push(async () => {
        const renderPromises = Array.from(document.querySelectorAll('.mermaid')).map(
            (element) => loadFromCacheOrRender(mermaid, element)
        );
        await Promise.all(renderPromises);
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

// Generates a percentage value for the width of the SVG based on its aspect ratio.
function calculateNewDiagramScale(svg) {
    const baseScale = 0.075;
    const minScale = 20;
    const maxScale = 100;

    const scale = baseScale * Math.max(svg.clientWidth, svg.clientHeight) + minScale;
    return Math.min(maxScale, scale);
}

// Only after rendering the diagrams, resize and center some misaligned elements.
function realignMermaidContents() {
    document.querySelectorAll('.mermaid').forEach(diagram => {
        diagram.style.width = '100%';
        const svg = diagram.querySelector('svg');
        svg.style.width = calculateNewDiagramScale(svg) + '%';
    });
    document.querySelectorAll('.mermaid foreignObject').forEach(obj => {
        obj.style.display = 'grid';
    });
}