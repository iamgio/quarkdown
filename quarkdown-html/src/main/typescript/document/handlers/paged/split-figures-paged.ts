import {SplitCaptionedElementsPaged} from "./split-captioned-elements-paged";

/**
 * Document handler that adjusts figures, such as captioned code blocks,
 * that have been split across page breaks, by moving their caption to the
 * portion matching its position: a top caption to the first portion,
 * a bottom caption to the last one.
 */
export class SplitFiguresPaged extends SplitCaptionedElementsPaged {
    protected readonly selector = 'figure';
}
