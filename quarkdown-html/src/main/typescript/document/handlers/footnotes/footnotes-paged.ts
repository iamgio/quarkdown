import {getOrCreateFootnoteRule} from "../../../footnotes/footnote-dom";
import {FootnotesDocumentHandler} from "./footnotes-document-handler";

/**
 * Footnote handler for paged documents that renders footnotes in a dedicated footnote area on each page.
 */
export class FootnotesPaged extends FootnotesDocumentHandler {
    /**
     * This is a hacky workaround for the base paged.js behavior:
     * Any change made after the pagination is done will not be processed by paged.js,
     * hence adding new content (footnotes) will cause content to overflow.
     *
     * This function takes all footnote references and creates a virtual empty space
     * of the size of the footnote definition, reserving space for it.
     * After rendering, `handleFootnotes` will remove this space and place
     * the footnote definition in the footnote area, balancing the layout.
     */
    async onPreRendering() {
        await super.onPreRendering();
        this.footnotes.forEach(({reference, definition}) =>{
            reference.style.display = 'block';
            reference.style.height = definition.scrollHeight + 'px';

            // Moves the footnote definition out of the page, to keep the layout intact.
            definition.remove();
            document.body.appendChild(definition);
        });
    }

    /**
     * Moves footnote definitions to their respective footnote areas,
     * and adjusts the layout accordingly.
     *
     * Useful context: https://github.com/pagedjs/pagedjs/issues/292
     */
    async onPostRendering() {
        await super.onPreRendering(); // Reloads footnotes pairs, since the DOM changed due to paged.js processing.
        this.footnotes.forEach(({reference, definition}) => {
            const pageArea = this.quarkdownDocument.getParentViewport(reference);
            console.log(document)
            if (!pageArea) return;
            const footnoteArea = pageArea.querySelector<HTMLElement>('.pagedjs_footnote_area > .pagedjs_footnote_content');
            if (!footnoteArea) return;
            const footnoteContent = footnoteArea.querySelector<HTMLElement>('.pagedjs_footnote_inner_content');
            if (!footnoteContent) return;

            // Moves the footnote definition to the footnote area.
            definition.remove();
            footnoteContent.appendChild(definition);

            footnoteArea.classList.remove('pagedjs_footnote_empty');
            footnoteContent.style.columnWidth = 'auto';
            pageArea.style.setProperty('--pagedjs-footnotes-height', `${footnoteArea.scrollHeight}px`);

            // Resets the temp properties set in pre-rendering.
            reference.style.height = 'auto';
            reference.style.display = 'inline';

            getOrCreateFootnoteRule(footnoteContent);
        });
    }
}