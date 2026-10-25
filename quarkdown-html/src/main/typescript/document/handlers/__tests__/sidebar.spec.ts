import {beforeEach, describe, expect, it} from 'vitest';
import {Sidebar} from "../sidebar";

const SELECTING_CLASS = 'selecting';

function pointer(type: string, target: EventTarget) {
    target.dispatchEvent(new MouseEvent(type, {bubbles: true}));
}

describe('Sidebar', () => {
    let sidebar: HTMLElement;
    let paragraph: HTMLElement;

    beforeEach(async () => {
        document.body.innerHTML = `
      <p id="text">Some text to select</p>
      <template id="sidebar-template">
        <nav class="sidebar"><ol><li data-target-id="a" data-depth="1"><a href="#a">A</a></li></ol></nav>
      </template>`;
        await new Sidebar({} as any).onPostRendering();
        sidebar = document.querySelector<HTMLElement>('nav.sidebar')!;
        paragraph = document.getElementById('text')!;
    });

    it('ignores the pointer while a selection drag started outside it is in progress', () => {
        pointer('pointerdown', paragraph);

        expect(sidebar.classList.contains(SELECTING_CLASS)).toBe(true);

        pointer('pointerup', document.body);

        expect(sidebar.classList.contains(SELECTING_CLASS)).toBe(false);
    });

    it('keeps reacting to the pointer when the drag starts on the sidebar itself', () => {
        pointer('pointerdown', sidebar.querySelector('a')!);

        expect(sidebar.classList.contains(SELECTING_CLASS)).toBe(false);
    });

    it('recovers when the window loses focus during the drag', () => {
        pointer('pointerdown', paragraph);
        window.dispatchEvent(new Event('blur'));

        expect(sidebar.classList.contains(SELECTING_CLASS)).toBe(false);
    });

    it('recovers when the drag is cancelled', () => {
        pointer('pointerdown', paragraph);
        pointer('pointercancel', document.body);

        expect(sidebar.classList.contains(SELECTING_CLASS)).toBe(false);
    });
});
