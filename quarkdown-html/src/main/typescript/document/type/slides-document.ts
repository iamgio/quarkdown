import {QuarkdownDocument} from "../quarkdown-document";
import {DocumentHandler} from "../document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {PageChunker} from "../../chunker/page-chunker";

declare const Reveal: typeof import("reveal.js"); // global Reveal at runtime
declare const RevealNotes: typeof import("reveal.js/plugin/notes/notes");

/**
 * Slides document implementation for Reveal.js presentations.
 */
export class SlidesDocument implements QuarkdownDocument {
    /**
     * Retrieves a configuration property from the global configuration (`slidesConfig`).
     * Configuration is injected by Quarkdown's `.slides` function.
     */
    private getConfigProperty<T>(property: string, defaultValue: T): T {
        const config = (window as any).slidesConfig || {};
        return config[property] ?? defaultValue;
    }

    /**
     * @returns The parent slide element of the given element.
     */
    getParentViewport(element: Element): Element | undefined {
        return element.closest(".reveal .slides > :is(section, .pdf-page)") || undefined;
    }

    /** Sets up pre-rendering to execute when DOM content is loaded */
    setupPreRenderingHook() {
        document.addEventListener("DOMContentLoaded", async () => await preRenderingExecutionQueue.execute());
    }

    /** Sets up post-rendering to execute when Reveal.js is ready */
    setupPostRenderingHook() {
        Reveal.addEventListener("ready", () => {
            if ((Reveal as any).isPrintView()) {
                Reveal.addEventListener("pdf-ready", () => postRenderingExecutionQueue.execute());
            } else {
                postRenderingExecutionQueue.execute().then();
            }
        });
    }

    /** Chunks content into slides and initializes Reveal.js */
    initializeRendering() {
        // Chunk the slides based on page breaks.
        const slidesDiv = document.querySelector<HTMLElement>('.reveal .slides');
        if (!slidesDiv) return;
        new PageChunker(slidesDiv).chunk();

        // Initialize Reveal.js with the updated DOM.
        Reveal.initialize({
            // If the center property is not explicitly set, it defaults to true unless the `--reveal-center-vertically` CSS variable of `:root` is set to `false`.
            center: this.getConfigProperty(
                "center",
                getComputedStyle(document.documentElement).getPropertyValue("--reveal-center-vertically") !== "false"
            ),
            controls: this.getConfigProperty("showControls", true),
            showNotes: this.getConfigProperty("showNotes", false),
            transition: this.getConfigProperty("transitionStyle", "slide"),
            transitionSpeed: this.getConfigProperty("transitionSpeed", "default"),
            hash: true,
            plugins: [RevealNotes],
        }).then();
    }

    getHandlers(): DocumentHandler[] {
        return [];
    }
}
