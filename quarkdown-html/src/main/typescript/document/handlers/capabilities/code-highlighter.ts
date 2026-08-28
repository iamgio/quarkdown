import {DocumentHandler} from "../../document-handler";
import {CodeLineNumbers} from "./code/line-numbers";
import {CodeFocus} from "./code/focus";
import {CodeCallouts} from "./code/callouts";
import {CodeCopyButton} from "./code/copy-button";

/**
 * Type declaration for the highlight.js library.
 */
declare const hljs: typeof import("highlight.js").default;

/**
 * A single enhancement feature applied to the document's code blocks
 * after highlight.js has completed syntax highlighting.
 */
export interface CodeFeature {
    /**
     * Applies this feature to all eligible code blocks in the document.
     */
    apply(): void;
}

/**
 * Document handler that provides syntax highlighting and code enhancement features.
 *
 * This handler integrates with highlight.js to provide:
 * - Syntax highlighting
 * - Line numbering ({@link CodeLineNumbers})
 * - Line focusing to emphasize specific code sections ({@link CodeFocus})
 * - Callout markers on specific lines ({@link CodeCallouts})
 * - Copy-to-clipboard ({@link CodeCopyButton})
 */
export class CodeHighlighter extends DocumentHandler {
    /**
     * Enhancement features applied after syntax highlighting.
     * The order matters: per-line features rely on the line structure
     * produced by the line numbers plugin.
     */
    private readonly features: CodeFeature[] = [
        new CodeLineNumbers(),
        new CodeFocus(),
        new CodeCallouts(),
    ];

    init() {
        new CodeCopyButton().register();
    }

    async onPostRendering() {
        hljs.highlightAll();
        this.features.forEach(feature => feature.apply());
    }
}
