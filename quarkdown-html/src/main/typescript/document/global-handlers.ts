import {DocumentHandler} from "./document-handler";
import {InlineCollapsibles} from "./handlers/inline-collapsibles";
import {QuarkdownDocument} from "./quarkdown-document";

/** Global document handlers that apply to all documents. */
export function getGlobalHandlers(document: QuarkdownDocument): DocumentHandler[] {
    return [
        new InlineCollapsibles(document),
        // TODO continue re-implementing script.js
    ]
}