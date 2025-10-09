import {DocumentHandler} from "./document-handler";
import {InlineCollapsiblesDocumentHandler} from "./handlers/inline-collapsibles";
import {QuarkdownDocument} from "./quarkdown-document";

/** Global document handlers that apply to all documents. */
export const globalHandlers: (doc: QuarkdownDocument) => DocumentHandler[] =
    doc => [
        new InlineCollapsiblesDocumentHandler(doc),
    ];