import {capabilities} from "../capabilities";
import {ConditionalDocumentHandler} from "./document-handler";
import {InlineCollapsibles} from "./handlers/inline-collapsibles";
import {QuarkdownDocument} from "./quarkdown-document";
import {RemainingHeight} from "./handlers/remaining-height";
import {MathRenderer} from "./handlers/capabilities/math-renderer";
import {CodeHighlighter} from "./handlers/capabilities/code-highlighter";

/** Global document handlers that apply to all documents. */
export function getGlobalHandlers(document: QuarkdownDocument): ConditionalDocumentHandler[] {
    return [
        new InlineCollapsibles(document),
        new RemainingHeight(document),
        capabilities.code && new CodeHighlighter(document),
        capabilities.math && new MathRenderer(document),
    ]
}