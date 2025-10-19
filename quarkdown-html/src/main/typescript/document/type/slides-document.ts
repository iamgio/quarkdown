import {DocumentHandler} from "../document-handler";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {PageChunker} from "../../chunker/page-chunker";
import {PageMarginsSlides} from "../handlers/page-margins/page-margins-slides";
import {FootnotesSlides} from "../handlers/footnotes/footnotes-slides";
import {PersistentHeadingsSlides} from "../handlers/persistent-headings/persistent-headings-slides";
import {PageNumbers} from "../handlers/page-numbers";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../paged-like-quarkdown-document";

declare const Reveal: typeof import("reveal.js"); // global Reveal at runtime
declare const RevealNotes: typeof import("reveal.js/plugin/notes/notes");

/**
 * A Reveal.js slide page, consisting of the slide and its background.
 */
type SlidesPage = { slide: HTMLElement, background: HTMLElement } & QuarkdownPage;

/**
 * Slides document implementation for Reveal.js presentations.
 */
export class SlidesDocument implements PagedLikeQuarkdownDocument<SlidesPage> {
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
    getParentViewport(element: Element): HTMLElement | undefined {
        return element.closest<HTMLElement>(".reveal .slides > :is(section, .pdf-page)") || undefined;
    }

    getPages(): SlidesPage[] {
        const slides = document.querySelector<HTMLElement>(".reveal .slides");
        const backgrounds = document.querySelector<HTMLElement>(".reveal .backgrounds");
        if (!slides || !backgrounds) return [];

        return Array.from(slides.children).map((slide, index) => {
            const background = backgrounds.children[index] as HTMLElement | undefined;
            const slideElement = slide as HTMLElement;

            return {
                slide: slideElement,
                background: background || document.createElement('div'), // Fallback for missing background
                querySelectorAll(query: string): NodeListOf<HTMLElement> {
                    const slideResults = slideElement.querySelectorAll<HTMLElement>(query);
                    const bgResults = background?.querySelectorAll<HTMLElement>(query) || [];
                    return new Set([...slideResults, ...bgResults]) as unknown as NodeListOf<HTMLElement>;
                }
            };
        });
    }

    getPageNumber(page: SlidesPage): number {
        const slide = page.slide;
        if (!slide.parentElement) return 0;
        const index = Array.from(slide.parentElement.children).indexOf(slide)
        return index + 1;
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
        return [
            new PageMarginsSlides(this),
            new PageNumbers(this),
            new FootnotesSlides(this),
            new PersistentHeadingsSlides(this),
        ];
    }
}
