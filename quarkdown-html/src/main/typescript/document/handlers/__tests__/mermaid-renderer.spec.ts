import {beforeEach, describe, expect, it, vi} from 'vitest';
import {MermaidRenderer} from "../capabilities/mermaid-renderer";

// mock mermaid
// @ts-ignore
globalThis.mermaid = {
    initialize: vi.fn(),
    render: vi.fn(async (id: string, code: string) => ({svg: `<svg id="${id}">${code}</svg>`}))
} as any;

class DummyDoc {
}

beforeEach(() => {
    sessionStorage.clear();
});

describe('MermaidRenderer', () => {
    it('renders and caches diagrams', async () => {
        const html = `<div class="mermaid">graph TD; A-->B;</div>`
        document.body.innerHTML = html;

        const handler = new MermaidRenderer(new DummyDoc() as any);

        // first render -> uses render()
        await handler.onPreRendering();

        const el = document.querySelector<HTMLElement>('.mermaid')!;
        expect(el.dataset.processed).toBe('true');
        expect(el.querySelector('svg')).toBeTruthy();

        document.body.innerHTML = html;

        // second render -> uses cache
        await handler.onPreRendering();
        expect((globalThis as any).mermaid.render).toHaveBeenCalledTimes(1);
    });
});
