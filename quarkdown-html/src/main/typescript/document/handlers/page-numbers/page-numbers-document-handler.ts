import {DocumentHandler} from "../../document-handler";

/**
 * Abstract base class for document handlers that manage page numbering.
 * Provides utility methods to find and update page number elements in documents.
 */
export abstract class PageNumbersDocumentHandler extends DocumentHandler {
    /**
     * Gets all elements that display the total page count.
     * @param element - The document or element to search within (default: document)
     * @returns NodeList of total page number elements (`.total-page-number`)
     */
    protected getTotalPageNumberElements(element: Document | Element = document): NodeListOf<HTMLElement> {
        return element.querySelectorAll<HTMLElement>('.total-page-number');
    }

    /**
     * Gets all elements that display the current page number.
     * @param element - The document or element to search within (default: document)
     * @returns NodeList of current page number elements (`.current-page-number`)
     */
    protected getCurrentPageNumberElements(element: Document | Element = document): NodeListOf<HTMLElement> {
        return element.querySelectorAll<HTMLElement>('.current-page-number');
    }
}