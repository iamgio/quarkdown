import {isBlank} from "../util/visibility";

/**
 * Utility that splits content into chunks based on page break elements.
 *
 * @example Input:
 *
 * ```html
 * <div class="slides">
 *     <p>First</p>
 *     <div class="page-break"></div>
 *     <p>Second</p>
 * </div>
 * ```
 *
 * Output:
 * 
 * ```html
 * <div class="slides">
 *     <section>
 *         <p>First</p>
 *      </section>
 *     <section>
 *         <p>Second</p>
 *     </section>
 * </div>
 * ```
 */
export class PageChunker {
    private container: HTMLElement;
    private chunks: HTMLElement[] = [];

    /** Initializes the chunker with the container element to be chunked. */
    constructor(container: HTMLElement) {
        this.container = container;
    }

    /**
     * Generates chunks based on the page break elements.
     * Page break elements are not preserved in the chunked output.
     * @param createElement Function that creates a new chunk element.
     */
    private generateChunks(createElement: () => HTMLElement): void {
        const chunks: HTMLElement[] = [];
        let currentChunk = createElement();

        Array.from(this.container.children).forEach((child: Element) => {
            const el = child as HTMLElement;
            if (el.className === "page-break") {
                // If we hit a page break, finalize the current section and start a new one.
                chunks.push(currentChunk);
                currentChunk = createElement();
            } else {
                // Otherwise, add the child to the current section.
                currentChunk.appendChild(child);
            }
        });

        // Add the last section if it has any content.
        if (currentChunk.childNodes.length > 0) {
            chunks.push(currentChunk);
        }

        this.chunks = chunks;
    }

    /** Applies the generated chunks to the container, replacing its content. */
    private apply(): void {
        // Clear out the original slides div and add the new sections.
        this.container.innerHTML = "";
        // Elements that are not part of a section yet and will be added to the next one.
        let queuedElements: Element[] = [];

        this.chunks.forEach((chunk: HTMLElement) => {
            // Empty chunks are ignored.
            if (isBlank(chunk)) {
                // If the section is blank and NOT empty,
                // they are added to the queued elements in order to be added to the next section
                // and not produce an empty chunk.
                queuedElements.push(...Array.from(chunk.children));
            } else {
                // If there are any queued elements, they are added to the beginning of the new section.
                if (queuedElements.length > 0) {
                    queuedElements.forEach(element => chunk.prepend(element));
                    queuedElements = [];
                }
                this.container.appendChild(chunk);
            }
        });

        // If there are any queued elements left, they are appended to the last visible section.
        if (queuedElements.length > 0 && this.chunks.length > 0) {
            const last = this.container.lastElementChild as HTMLElement | null;
            if (last) {
                queuedElements.forEach(element => last.appendChild(element));
            }
            queuedElements = [];
        }
    }

    /**
     * Chunks the container into sections based on page breaks.
     * Page breaks are not preserved in the output, and empty chunks are ignored.
     * The container's content is replaced with the chunked sections.
     * @param chunkTagName The tag name to use for chunk elements (default is "section").
     */
    chunk(chunkTagName: string = "section"): void {
        const createElement = (): HTMLElement => {
            const element = document.createElement(chunkTagName);
            element.className = "chunk";
            return element;
        };
        this.generateChunks(createElement);
        this.apply();
    }
}