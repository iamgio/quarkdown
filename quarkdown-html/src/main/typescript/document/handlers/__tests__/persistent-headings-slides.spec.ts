import {describe, expect, it} from 'vitest';
import {PersistentHeadingsSlides} from "../persistent-headings/persistent-headings-slides";

class DummyDoc {
}

describe('PersistentHeadingsSlides', () => {
    it('applies persistent headings to slides and backgrounds', async () => {
        document.body.innerHTML = `
      <div class="reveal">
        <div class="slides">
          <section>
            <h1>Title 1</h1>
          </section>
          <section>
            <h2>Title 2</h2>
            <div class="last-heading" data-depth="1"></div>
            <div class="last-heading" data-depth="2"></div>
          </section>
        </div>
        <div class="backgrounds">
          <div class="slide-background">
            <div class="last-heading" data-depth="1"></div>
          </div>
          <div class="slide-background">
            <div class="last-heading" data-depth="1"></div>
            <div class="last-heading" data-depth="2"></div>
          </div>
        </div>
      </div>`;

        const handler = new PersistentHeadingsSlides(new DummyDoc() as any);
        await handler.onPostRendering();

        const slides = document.querySelectorAll('.reveal .slides > section');
        const backgrounds = document.querySelectorAll('.reveal > .backgrounds > .slide-background');

        // Slide 2 and its background should have last h1 and h2 applied
        const slide2H1 = slides[1].querySelector<HTMLElement>('[data-depth="1"]')!;
        const slide2H2 = slides[1].querySelector<HTMLElement>('[data-depth="2"]')!;
        expect(slide2H1.innerHTML).toBe('Title 1');
        expect(slide2H2.innerHTML).toBe('Title 2');

        const bg2H1 = backgrounds[1].querySelector<HTMLElement>('[data-depth="1"]')!;
        const bg2H2 = backgrounds[1].querySelector<HTMLElement>('[data-depth="2"]')!;
        expect(bg2H1.innerHTML).toBe('Title 1');
        expect(bg2H2.innerHTML).toBe('Title 2');
    });
});
