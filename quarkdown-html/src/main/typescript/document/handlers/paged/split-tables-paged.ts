import {SplitCaptionedElementsPaged} from "./split-captioned-elements-paged";

/**
 * Document handler that adjusts tables that have been split across page breaks,
 * by moving their caption to the portion matching its position,
 * as provided by [SplitCaptionedElementsPaged].
 */
export class SplitTablesPaged extends SplitCaptionedElementsPaged<HTMLTableElement> {
    protected readonly selector = 'table';
}
