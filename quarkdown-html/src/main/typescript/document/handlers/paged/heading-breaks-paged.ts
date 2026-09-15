import {DocumentHandler} from "../../document-handler";

/**
 * Elements that may span multiple pages when split across page breaks.
 */
const MULTI_PAGE_SELECTOR = 'pre, table, figure';

/**
 * Document handler that allows page breaks between a heading and a following element
 * that may span multiple pages.
 *
 *  Before pagination, this handler marks such headings with the `allow-break-after`
 * class, which relaxes the constraint via CSS.
 */
export class HeadingBreaksPaged extends DocumentHandler {
    async onPreRendering() {
        document.querySelectorAll<HTMLElement>('h1, h2, h3, h4, h5, h6').forEach(heading => {
            if (heading.nextElementSibling?.matches(MULTI_PAGE_SELECTOR)) {
                heading.classList.add('allow-break-after');
            }
        });
    }
}
