import {DocumentHandler} from "../document-handler";

/**
 * Type declaration for the KaTeX library used for rendering mathematical formulas.
 */
declare const katex: typeof import("katex");

/**
 * Global window interface extension to support TeX macro definitions.
 */
declare global {
    interface Window {
        /** Optional object containing TeX macro definitions for mathematical formulas */
        texMacros?: {[key: string]: string};
    }
}

/**
 * Document handler that processes and renders mathematical formulas using KaTeX.
 * 
 * This handler converts LaTeX mathematical expressions found in `<formula>` elements
 * into rendered HTML using the KaTeX library. It supports both inline and block-level
 * mathematical expressions and can utilize custom TeX macros if defined globally.
 * 
 * The handler operates during the pre-rendering phase to ensure mathematical
 * content is processed before document rendering is finalized.
 */
export class Math extends DocumentHandler {
    onPreRendering() {
        const texMacros = window.texMacros;
        const formulas = document.querySelectorAll<HTMLElement>('formula');

        formulas.forEach((formula) => {
            const content = formula.textContent;
            const isBlock = formula.dataset.block === '';
            if (!content) return;

            formula.innerHTML = katex.renderToString(content, {
                throwOnError: false,
                displayMode: isBlock,
                macros: texMacros || {},
            });
        });
    }
}