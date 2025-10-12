import {DocumentHandler} from "../../document-handler";
import {hashCode} from "../../../util/hash";

/**
 * Type declaration for the Mermaid library used for rendering diagrams.
 */
declare const mermaid: typeof import("mermaid").default;

/**
 * Document handler that renders Mermaid diagrams from textual descriptions.
 *
 * This handler processes elements with the 'mermaid' class and converts their
 * text content into rendered SVG diagrams using the Mermaid library.
 * It includes caching mechanisms for performance and automatic scaling for better presentation.
 */
export class MermaidRenderer extends DocumentHandler {
    init() {
        mermaid.initialize({startOnLoad: false});
    }

    /** Processes all Mermaid diagrams in the document. */
    async onPreRendering() {
        const diagrams = document.querySelectorAll<HTMLElement>('.mermaid:not([data-processed])');
        const renderPromises = Array.from(diagrams).map(
            (element) => this.loadFromCacheOrRender(element)
        );
        await Promise.all(renderPromises)
        this.realignDiagramContents();
    }

    /**
     * Renders a single Mermaid diagram element, using cached results when available.
     *
     * The caching mechanism uses session storage with a hash of the diagram content
     * as the key. This ensures that identical diagrams are only rendered once per
     * browser session, significantly improving performance for documents with
     * repeated or unchanged diagrams.
     *
     * @param element The HTML element containing the Mermaid diagram text
     */
    private async loadFromCacheOrRender(element: HTMLElement) {
        const code = element.textContent?.trim() || '';
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
        console.log(diagram);
        const svg = diagram.svg;
        element.innerHTML = svg;
        sessionStorage.setItem(id, svg);
    }

    /**
     * Calculates an appropriate scale percentage for a diagram based on its aspect ratio.
     *
     * Uses a scaling formula that considers the diagram's width-to-height ratio
     * to determine an optimal display size. Wider diagrams get larger scales while
     * taller diagrams are kept more compact.
     *
     * @param svg The SVG element containing the rendered diagram
     * @returns A percentage value (0-100) representing the optimal scale
     */
    private calculateNewDiagramScale(svg: SVGSVGElement) {
        const scaleFactor = 0.2;
        const scaleOffset = 0.4;
        const maxScale = 100;

        const width = svg.viewBox.baseVal.width || svg.clientWidth || 1;
        const height = svg.viewBox.baseVal.height || svg.clientHeight || 1;
        const aspectRatio = width / height;

        const scale = (scaleOffset + scaleFactor * aspectRatio) * maxScale;
        return Math.min(maxScale, scale);
    }

    /**
     * Applies styling adjustments to improve diagram presentation and alignment.
     */
    private realignDiagramContents() {
        document.querySelectorAll<HTMLElement>('.mermaid').forEach(diagram => {
            diagram.style.width = '100%';
            const svg = diagram.querySelector('svg');
            if (!svg) return;
            svg.style.width = this.calculateNewDiagramScale(svg) + '%';
        });
        document.querySelectorAll<HTMLElement>('.mermaid foreignObject').forEach(obj => {
            obj.style.display = 'grid';
        });
    }
}