import {DocumentHandler} from "../document-handler";

/** Moves generated styles out of document content before rendering. */
export class StyleHeadRelocator extends DocumentHandler {
    async onPreRendering() {
        document
            .querySelectorAll<HTMLStyleElement>('body style[data-hidden]')
            .forEach(style => document.head.appendChild(style));
    }
}
