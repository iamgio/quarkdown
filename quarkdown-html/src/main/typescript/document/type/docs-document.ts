import {DocumentHandler} from "../document-handler";
import {PlainDocument} from "./plain-document";
import {PageMarginsDocs} from "../handlers/page-margins/page-margins-docs";
import {SearchFieldFocus} from "../handlers/docs/search-field-focus";
import {SearchField} from "../handlers/docs/search-field";

/**
 * 'Docs' document implementation for HTML documents targeting documentation sites and wikis.
 * This implementation relies on PlainDocument for most functionality.
 */
export class DocsDocument extends PlainDocument {
    getHandlers(): DocumentHandler[] {
        return [
            new SearchFieldFocus(this),
            new SearchField(this),
            new PageMarginsDocs(this)
        ];
    }
}
