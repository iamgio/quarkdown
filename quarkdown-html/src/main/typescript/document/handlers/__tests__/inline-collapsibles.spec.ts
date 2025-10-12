import {describe, expect, it} from 'vitest';
import {InlineCollapsibles} from "../inline-collapsibles";

class DummyDoc {
}

describe('InlineCollapsibles', () => {
    it('toggles between full and collapsed content using innerHTML for user-defined', async () => {
        document.body.innerHTML = `
      <span class="inline-collapse" data-full-text="FULL <b>X</b>" data-collapsed-text="COLL" data-collapsed="true"></span>`;

        const h = new InlineCollapsibles(new DummyDoc() as any);
        await h.onPostRendering();

        const span = document.querySelector('.inline-collapse') as HTMLElement;

        // Click should toggle to full text (since it was collapsed)
        span.click();
        expect(span.dataset.collapsed).toBe('false');
        expect(span.innerHTML).toBe('FULL <b>X</b>');

        // Click back should collapse and set innerHTML to collapsed text
        span.click();
        expect(span.dataset.collapsed).toBe('true');
        expect(span.innerHTML).toBe('COLL');
    });

    it('uses textContent when inside .error', async () => {
        document.body.innerHTML = `
      <div class="error">
        <span class="inline-collapse" data-full-text="<i>FULL</i>" data-collapsed-text="COLL" data-collapsed="true"></span>
      </div>`;

        const handler = new InlineCollapsibles(new DummyDoc() as any);
        await handler.onPostRendering();

        const span = document.querySelector('.inline-collapse') as HTMLElement;
        span.click();
        // inside error -> textContent should be used, not interpreted HTML (literal string with tags)
        expect(span.textContent).toBe('<i>FULL</i>');

        span.click();
        expect(span.textContent).toBe('COLL');
    });
});
