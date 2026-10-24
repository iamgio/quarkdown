import {FootnotesDocumentHandler} from "./footnotes-document-handler";
import {DetachedFootnotes} from "./detached-footnotes";
import {QuarkdownDocument} from "../../quarkdown-document";

/**
 * Footnote handler for paged documents.
 *
 * Definitions are taken out of the content flow before pagination and placed into the footnote area
 * of the page of their first reference while paged.js lays that page out (see `PlaceFootnotes`),
 * so that the space they take is exact and accounted for by the pagination itself.
 */
export class FootnotesPaged extends FootnotesDocumentHandler {
    /**
     * @param quarkdownDocument the document this handler manages
     * @param detached store the definitions are detached into, shared with the node handler placing them
     */
    constructor(quarkdownDocument: QuarkdownDocument, private readonly detached: DetachedFootnotes) {
        super(quarkdownDocument);
    }

    async onPreRendering() {
        await super.onPreRendering();
        this.detached.detach(this.footnotes);
    }
}
