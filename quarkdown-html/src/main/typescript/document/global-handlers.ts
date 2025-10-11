import {ConditionalDocumentHandler} from "./document-handler";
import {InlineCollapsibles} from "./handlers/inline-collapsibles";
import {QuarkdownDocument} from "./quarkdown-document";
import {RemainingHeight} from "./handlers/remaining-height";
import {MathRenderer} from "./handlers/capabilities/math-renderer";
import {capabilities} from "../capabilities";

/** Global document handlers that apply to all documents. */
export function getGlobalHandlers(document: QuarkdownDocument): ConditionalDocumentHandler[] {
    return [
        new InlineCollapsibles(document),
        new RemainingHeight(document),
        capabilities.math && new MathRenderer(document),
    ]
}