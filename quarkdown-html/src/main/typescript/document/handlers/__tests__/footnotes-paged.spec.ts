import {beforeEach, describe, expect, it} from 'vitest';
import {FootnotesPaged} from "../footnotes/footnotes-paged";

/**
 * Stubs the definition's `scrollHeight` with a fixed value,
 * recording each read in the shared access log.
 */
function stubScrollHeight(definition: HTMLElement, height: number, log: string[]) {
    Object.defineProperty(definition, 'scrollHeight', {
        get: () => {
            log.push(`read:${definition.id}`);
            return height;
        },
    });
}

describe('FootnotesPaged', () => {
    beforeEach(() => {
        document.body.innerHTML = `
      <main>
        <p>A<span class="footnote-reference" data-definition="def-1">1</span></p>
        <div class="footnote-definition" id="def-1" data-footnote-index="1">First</div>
        <p>B<span class="footnote-reference" data-definition="def-2">2</span></p>
        <div class="footnote-definition" id="def-2" data-footnote-index="2">Second</div>
      </main>`;
    });

    it('reserves space for each definition at its reference on pre-rendering', async () => {
        const log: string[] = [];
        stubScrollHeight(document.getElementById('def-1')!, 30, log);
        stubScrollHeight(document.getElementById('def-2')!, 45, log);

        await new FootnotesPaged({} as any).onPreRendering();

        const references = document.querySelectorAll<HTMLElement>('.footnote-reference');
        expect(references[0].style.display).toBe('block');
        expect(references[0].style.height).toBe('30px');
        expect(references[1].style.height).toBe('45px');
    });

    it('moves definitions out of the content flow on pre-rendering', async () => {
        await new FootnotesPaged({} as any).onPreRendering();

        expect(document.querySelectorAll('main .footnote-definition')).toHaveLength(0);
        expect(document.querySelectorAll('body > .footnote-definition')).toHaveLength(2);
    });

    it('measures all definitions before mutating the DOM', async () => {
        const log: string[] = [];
        stubScrollHeight(document.getElementById('def-1')!, 30, log);
        stubScrollHeight(document.getElementById('def-2')!, 45, log);
        const originalAppend = document.body.appendChild.bind(document.body);
        document.body.appendChild = <T extends Node>(node: T): T => {
            log.push(`write:${(node as unknown as HTMLElement).id}`);
            return originalAppend(node);
        };

        await new FootnotesPaged({} as any).onPreRendering();

        expect(log).toEqual(['read:def-1', 'read:def-2', 'write:def-1', 'write:def-2']);
    });
});
