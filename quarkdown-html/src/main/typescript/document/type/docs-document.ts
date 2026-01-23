import {DocumentHandler} from "../document-handler";
import {PlainDocument} from "./plain-document";
import {PageMarginsDocs} from "../handlers/page-margins/page-margins-docs";
import {SearchFieldFocus} from "../handlers/docs/search-field-focus";
import {SearchField} from "../handlers/docs/search-field";
import {FootnotesDocs} from "../handlers/footnotes/footnotes-docs";
import {SiblingPagesButtons} from "../handlers/docs/sibling-pages-buttons";

/**
 * 'Docs' document implementation for HTML documents targeting documentation sites and wikis.
 * This implementation relies on PlainDocument for most functionality.
 */
export class DocsDocument extends PlainDocument {
    getHandlers(): DocumentHandler[] {
        return [
            new SearchFieldFocus(this),
            new SearchField(this),
            new SiblingPagesButtons(this),
            new PageMarginsDocs(this),
            new FootnotesDocs(this),
        ];
    }
}
