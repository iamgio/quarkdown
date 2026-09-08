import {beforeEach, describe, expect, it} from 'vitest';
import {ScrollableTables} from "../scrollable-tables";

class DummyDoc {
}

async function run() {
    await new ScrollableTables(new DummyDoc() as any).onPostRendering();
}

describe('ScrollableTables', () => {
    beforeEach(() => {
        document.body.className = 'quarkdown quarkdown-docs';
        document.body.innerHTML = '';
    });

    it('wraps a content table in a scroll area, preserving its position', async () => {
        document.body.innerHTML = `
            <div class="content-wrapper">
                <main>
                    <p>Before</p>
                    <table><tbody><tr><td>Cell</td></tr></tbody></table>
                    <p>After</p>
                </main>
            </div>`;

        await run();

        const wrapper = document.querySelector('main > .table-scroll-area');
        expect(wrapper).not.toBeNull();
        expect(wrapper!.querySelector('table')).not.toBeNull();
        expect(wrapper!.previousElementSibling?.textContent).toBe('Before');
        expect(wrapper!.nextElementSibling?.textContent).toBe('After');
    });

    it('does not wrap tables inside code blocks', async () => {
        document.body.innerHTML = `
            <div class="content-wrapper">
                <main>
                    <pre><code><table><tbody><tr><td>1</td></tr></tbody></table></code></pre>
                </main>
            </div>`;

        await run();

        expect(document.querySelector('.table-scroll-area')).toBeNull();
    });

    it('does not wrap tables outside the main content area', async () => {
        document.body.innerHTML = `
            <div class="content-wrapper">
                <aside><table><tbody><tr><td>Nav</td></tr></tbody></table></aside>
                <main></main>
            </div>`;

        await run();

        expect(document.querySelector('.table-scroll-area')).toBeNull();
    });

    it('does not wrap the same table twice', async () => {
        document.body.innerHTML = `
            <div class="content-wrapper">
                <main>
                    <table><tbody><tr><td>Cell</td></tr></tbody></table>
                </main>
            </div>`;

        await run();
        await run();

        expect(document.querySelectorAll('.table-scroll-area').length).toBe(1);
        expect(document.querySelector('.table-scroll-area .table-scroll-area')).toBeNull();
    });
});
