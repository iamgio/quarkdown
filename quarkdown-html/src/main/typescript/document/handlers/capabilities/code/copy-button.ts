/**
 * Type declaration for the highlight.js library.
 */
declare const hljs: typeof import("highlight.js").default;

/**
 * Type declaration for the copy button plugin used with highlight.js.
 */
declare const CopyButtonPlugin: { new(...args: any[]): any };

/**
 * Copy-to-clipboard button for code blocks, backed by the highlight.js copy button plugin.
 */
export class CodeCopyButton {
    /**
     * Registers the copy button plugin, so that every block highlighted
     * afterwards gains a copy button.
     */
    register() {
        hljs.addPlugin(new CopyButtonPlugin());
    }
}
