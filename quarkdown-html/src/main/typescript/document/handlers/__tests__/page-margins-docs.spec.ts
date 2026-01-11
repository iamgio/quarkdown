import {beforeEach, describe, expect, it} from 'vitest';
import {PageMarginsDocs} from "../page-margins/page-margins-docs";
import {QuarkdownDocument} from "../../quarkdown-document";
import {DocumentHandler} from "../../document-handler";

class DummyDocument implements QuarkdownDocument {
    getParentViewport(): HTMLElement | undefined {
        return undefined;
    }

    setupPreRenderingHook(): void {
    }

    setupPostRenderingHook(): void {
    }

    initializeRendering(): void {
    }

    getHandlers(): DocumentHandler[] {
        return [];
    }
}

const createInitializer = (position: string, content: string = '', extraClass: string = ''): string => {
    const classes = `page-margin-content${extraClass ? ` ${extraClass}` : ''}`;
    return `<div class="${classes}" data-on-left-page="${position}" data-on-right-page="${position}">${content}</div>`;
};

const setupDocsStructure = (initializers: string = ''): void => {
    document.body.innerHTML = `
        <header>
            <aside class="margin-area"></aside>
            <main></main>
            <aside class="margin-area"></aside>
        </header>
        <div class="content-wrapper">
            <aside class="margin-area">
                <div class="position-top"></div>
                <div class="position-middle"></div>
                <div class="position-bottom"></div>
            </aside>
            <main>
                ${initializers}
                <footer>
                    <div class="position-left"></div>
                    <div class="position-center"></div>
                    <div class="position-right"></div>
                </footer>
            </main>
            <aside class="margin-area">
                <div class="position-top"></div>
                <div class="position-middle"></div>
                <div class="position-bottom"></div>
            </aside>
        </div>
    `;
};

describe('PageMarginsDocs', () => {
    let handler: PageMarginsDocs;

    beforeEach(() => {
        handler = new PageMarginsDocs(new DummyDocument());
    });

    describe('header positions', () => {
        it('moves top-left-corner to header left aside', async () => {
            setupDocsStructure(createInitializer('top-left-corner', 'TLC'));
            await handler.onPostRendering();

            const target = document.querySelector('header > aside:first-child');
            expect(target?.querySelector('.page-margin-top-left-corner')?.textContent).toBe('TLC');
        });

        it('moves top-left to header left aside', async () => {
            setupDocsStructure(createInitializer('top-left', 'TL'));
            await handler.onPostRendering();

            const target = document.querySelector('header > aside:first-child');
            expect(target?.querySelector('.page-margin-top-left')?.textContent).toBe('TL');
        });

        it('moves top-center to header main', async () => {
            setupDocsStructure(createInitializer('top-center', 'TC'));
            await handler.onPostRendering();

            const target = document.querySelector('header > main');
            expect(target?.querySelector('.page-margin-top-center')?.textContent).toBe('TC');
        });

        it('moves top-right-corner to header right aside', async () => {
            setupDocsStructure(createInitializer('top-right-corner', 'TRC'));
            await handler.onPostRendering();

            const target = document.querySelector('header > aside:last-child');
            expect(target?.querySelector('.page-margin-top-right-corner')?.textContent).toBe('TRC');
        });

        it('moves top-right to header right aside', async () => {
            setupDocsStructure(createInitializer('top-right', 'TR'));
            await handler.onPostRendering();

            const target = document.querySelector('header > aside:last-child');
            expect(target?.querySelector('.page-margin-top-right')?.textContent).toBe('TR');
        });
    });

    describe('left sidebar positions', () => {
        it('moves left-top to sidebar position-top', async () => {
            setupDocsStructure(createInitializer('left-top', 'LT'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:first-child > .position-top');
            expect(target?.querySelector('.page-margin-left-top')?.textContent).toBe('LT');
        });

        it('moves left-middle to sidebar position-middle', async () => {
            setupDocsStructure(createInitializer('left-middle', 'LM'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:first-child > .position-middle');
            expect(target?.querySelector('.page-margin-left-middle')?.textContent).toBe('LM');
        });

        it('moves left-bottom to sidebar position-bottom', async () => {
            setupDocsStructure(createInitializer('left-bottom', 'LB'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:first-child > .position-bottom');
            expect(target?.querySelector('.page-margin-left-bottom')?.textContent).toBe('LB');
        });

        it('moves bottom-left-corner to sidebar position-bottom', async () => {
            setupDocsStructure(createInitializer('bottom-left-corner', 'BLC'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:first-child > .position-bottom');
            expect(target?.querySelector('.page-margin-bottom-left-corner')?.textContent).toBe('BLC');
        });
    });

    describe('right sidebar positions', () => {
        it('moves right-top to sidebar position-top', async () => {
            setupDocsStructure(createInitializer('right-top', 'RT'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:last-child > .position-top');
            expect(target?.querySelector('.page-margin-right-top')?.textContent).toBe('RT');
        });

        it('moves right-middle to sidebar position-middle', async () => {
            setupDocsStructure(createInitializer('right-middle', 'RM'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:last-child > .position-middle');
            expect(target?.querySelector('.page-margin-right-middle')?.textContent).toBe('RM');
        });

        it('moves right-bottom to sidebar position-bottom', async () => {
            setupDocsStructure(createInitializer('right-bottom', 'RB'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:last-child > .position-bottom');
            expect(target?.querySelector('.page-margin-right-bottom')?.textContent).toBe('RB');
        });

        it('moves bottom-right-corner to sidebar position-bottom', async () => {
            setupDocsStructure(createInitializer('bottom-right-corner', 'BRC'));
            await handler.onPostRendering();

            const target = document.querySelector('.content-wrapper > aside:last-child > .position-bottom');
            expect(target?.querySelector('.page-margin-bottom-right-corner')?.textContent).toBe('BRC');
        });
    });

    describe('footer positions', () => {
        it('moves bottom-left to footer position-left', async () => {
            setupDocsStructure(createInitializer('bottom-left', 'BL'));
            await handler.onPostRendering();

            const target = document.querySelector('footer > .position-left');
            expect(target?.querySelector('.page-margin-bottom-left')?.textContent).toBe('BL');
        });

        it('moves bottom-center to footer position-center', async () => {
            setupDocsStructure(createInitializer('bottom-center', 'BC'));
            await handler.onPostRendering();

            const target = document.querySelector('footer > .position-center');
            expect(target?.querySelector('.page-margin-bottom-center')?.textContent).toBe('BC');
        });

        it('moves bottom-right to footer position-right', async () => {
            setupDocsStructure(createInitializer('bottom-right', 'BR'));
            await handler.onPostRendering();

            const target = document.querySelector('footer > .position-right');
            expect(target?.querySelector('.page-margin-bottom-right')?.textContent).toBe('BR');
        });
    });

    describe('wrapper creation', () => {
        it('preserves extra classes from initializer', async () => {
            setupDocsStructure(createInitializer('top-center', 'content', 'custom-class another-class'));
            await handler.onPostRendering();

            const wrapper = document.querySelector('header > main > div');
            expect(wrapper?.classList.contains('page-margin-top-center')).toBe(true);
            expect(wrapper?.classList.contains('page-margin-content')).toBe(true);
            expect(wrapper?.classList.contains('custom-class')).toBe(true);
            expect(wrapper?.classList.contains('another-class')).toBe(true);
        });

        it('preserves HTML content', async () => {
            setupDocsStructure(createInitializer('top-center', '<strong>Bold</strong> text'));
            await handler.onPostRendering();

            const wrapper = document.querySelector('header > main > .page-margin-top-center');
            expect(wrapper?.innerHTML).toBe('<strong>Bold</strong> text');
        });
    });

    describe('cleanup', () => {
        it('removes initializers after processing', async () => {
            setupDocsStructure(createInitializer('top-center', 'content'));
            await handler.onPostRendering();

            const remaining = document.querySelectorAll('.content-wrapper > main > .page-margin-content');
            expect(remaining.length).toBe(0);
        });

        it('removes initializers with unknown positions', async () => {
            setupDocsStructure(createInitializer('unknown-position', 'content'));
            await handler.onPostRendering();

            const remaining = document.querySelectorAll('.page-margin-content');
            expect(remaining.length).toBe(0);
        });
    });

    describe('multiple margins', () => {
        it('handles multiple margins in different positions', async () => {
            setupDocsStructure(`
                ${createInitializer('top-left', 'TL')}
                ${createInitializer('bottom-center', 'BC')}
                ${createInitializer('right-middle', 'RM')}
            `);
            await handler.onPostRendering();

            expect(document.querySelector('header > aside:first-child > .page-margin-top-left')?.textContent).toBe('TL');
            expect(document.querySelector('footer > .position-center > .page-margin-bottom-center')?.textContent).toBe('BC');
            expect(document.querySelector('.content-wrapper > aside:last-child > .position-middle > .page-margin-right-middle')?.textContent).toBe('RM');
        });
    });
});
