import {DocumentHandler} from "../../document-handler";
import {FootnotePair} from "../../../footnotes/footnote-pair";
import {getFootnoteDefinitionsAndFirstReference} from "../../../footnotes/footnote-lookup";

/**
 * Abstract base class for document handlers that work with footnotes.
 * Automatically collects footnote pairs during pre-rendering phase.
 */
export abstract class FootnotesDocumentHandler extends DocumentHandler {
    /** Footnote pairs (reference + definition) collected during pre-rendering. */
    protected footnotes: FootnotePair[] = [];

    async onPreRendering() {
        this.footnotes = getFootnoteDefinitionsAndFirstReference();
    }
}