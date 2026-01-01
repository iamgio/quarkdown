import {QuarkdownDocument} from "../quarkdown-document";
import {DocumentHandler} from "../document-handler";
import {Sidebar} from "../handlers/sidebar";
import {postRenderingExecutionQueue, preRenderingExecutionQueue} from "../../queue/execution-queues";
import {FootnotesPlain} from "../handlers/footnotes/footnotes-plain";
import {PlainDocument} from "./plain-document";
import {PageMarginsDocs} from "../handlers/page-margins/page-margins-docs";

/**
 * 'Docs' document implementation for HTML documents targeting documentation sites and wikis.
 * This implementation relies on PlainDocument for most functionality.
 */
export class DocsDocument extends PlainDocument {
    getHandlers(): DocumentHandler[] {
        return [
            new PageMarginsDocs(this),
        ];
    }
}
