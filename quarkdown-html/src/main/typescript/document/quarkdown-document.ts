import {DocumentHandler} from "./document-handler";
import {preRenderingExecutionQueue} from "../queue/execution-queues";
import {getGlobalHandlers} from "./global-handlers";

/**
 * Core interface for Quarkdown document types.
 * Defines the document rendering lifecycle and management.
 */
export interface QuarkdownDocument {
    /**
     * Gets the parent viewport element for the given element.
     * For instance, the element's slide in `slides` documents,
     * or the element's page in `paged` documents.
     * @param element - The element to find the parent viewport for
     * @returns The parent viewport element, if any
     */
    getParentViewport(element: Element): HTMLElement | undefined;

    /** Sets up the hook that executed the pre-rendering queue. */
    setupPreRenderingHook(): void;

    /** Sets up the hook that executes the post-rendering queue. */
    setupPostRenderingHook(): void;

    /**
     * Initializes the document rendering process.
     * For instance, Reveal.js inizialization in `slides` documents.
     */
    initializeRendering(): void;

    /**
     * @returns Array of document handlers that apply to this document
     */
    getHandlers(): DocumentHandler[]
}

/**
 * Prepares a Quarkdown document for rendering by setting up handlers and hooks.
 * This is called by the HTML wrapper.
 * @param document - The document to prepare for rendering
 */
export function prepare(document: QuarkdownDocument): void {
    const handlers = [...getGlobalHandlers(document), ...document.getHandlers()];
    handlers.forEach(handler => handler.pushToQueue());

    document.setupPreRenderingHook();
    document.setupPostRenderingHook();
    preRenderingExecutionQueue.addOnComplete(() => document.initializeRendering());
}
