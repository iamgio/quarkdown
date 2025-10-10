import {DocumentHandler} from "./document-handler";
import {InlineCollapsibles} from "./handlers/inline-collapsibles";
import {QuarkdownDocument} from "./quarkdown-document";
import {RemainingHeight} from "./handlers/remaining-height";

/** Global document handlers that apply to all documents. */
export function getGlobalHandlers(document: QuarkdownDocument): DocumentHandler[] {
    return [
        new InlineCollapsibles(document),
        new RemainingHeight(document),
    ]
}