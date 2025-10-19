import {describe, expect, it} from 'vitest';
import {PageNumbers} from "../page-numbers";

class DummyDoc {
}

class Concrete extends PageNumbers {
}

describe('PageNumbersDocumentHandler', () => {
    it('finds total and current page number elements', () => {
        document.body.innerHTML = `
      <div>
        <span class="total-page-number">T1</span>
        <span class="current-page-number">C1</span>
        <span class="current-page-number">C2</span>
      </div>`;

        const handler = new Concrete(new DummyDoc() as any);
        // @ts-expect-error Testing protected methods via casting
        const totals = handler.getTotalPageNumberElements(document as any);
        // @ts-expect-error Testing protected methods via casting
        const currents = handler.getCurrentPageNumberElements(document as any);

        expect(Array.from(totals).map(e => e.textContent)).toEqual(['T1']);
        expect(currents.length).toBe(2);
    });
});
