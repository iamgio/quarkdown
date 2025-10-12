import {describe, expect, it} from 'vitest';
import {RemainingHeight} from "../remaining-height";

class DocStub {
    getParentViewport(el: HTMLElement): HTMLElement | null {
        return document.querySelector('#viewport');
    }
}

describe('RemainingHeight', () => {
    it('sets --viewport-remaining-height on .fill-height elements', async () => {
        document.body.innerHTML = `
      <div id="viewport"></div>
      <div class="fill-height" id="content"></div>`;

        const viewport = document.getElementById('viewport') as any;
        const content = document.getElementById('content') as any;

        // Mock layout boxes
        viewport.getBoundingClientRect = () => ({
            top: 0,
            left: 0,
            right: 0,
            bottom: 800,
            width: 0,
            height: 800,
            x: 0,
            y: 0,
            toJSON() {
            }
        } as any);
        content.getBoundingClientRect = () => ({
            top: 300,
            left: 0,
            right: 0,
            bottom: 0,
            width: 0,
            height: 0,
            x: 0,
            y: 0,
            toJSON() {
            }
        } as any);

        const handler = new RemainingHeight(new DocStub() as any);
        await handler.onPostRendering();

        const value = (content as HTMLElement).style.getPropertyValue('--viewport-remaining-height');
        expect(value).toBe('500px');
    });
});
