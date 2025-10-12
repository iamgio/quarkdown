import {describe, expect, it} from 'vitest';
import {
    PersistentHeadingsDocumentHandler
} from '../../../../document/handlers/persistent-headings/persistent-headings-document-handler';

class DummyDoc {}

class Concrete extends PersistentHeadingsDocumentHandler {
  public applyProxy(params: { sourceContainer: Element; targetContainers: Element[] }) {
    // @ts-expect-error access to protected method for testing
    return this.apply(params);
  }
}

describe('PersistentHeadingsDocumentHandler', () => {
  it('stores last heading per depth and applies to .last-heading', () => {
    // Build a page with two containers: source and target
    const source = document.createElement('div');
    source.innerHTML = `
      <h1>Main</h1>
      <h2>Section A</h2>
      <h3 data-decorative>Decor</h3>
      <h2>Section B</h2>`; // last h2 should win

    const target = document.createElement('div');
    target.innerHTML = `
      <div class="last-heading" data-depth="1"></div>
      <div class="last-heading" data-depth="2"></div>
      <div class="last-heading" data-depth="3"></div>`;

    const handler = new Concrete(new DummyDoc() as any);
    handler.applyProxy({ sourceContainer: source, targetContainers: [target] });

    const [h1, h2, h3] = Array.from(target.querySelectorAll('.last-heading')) as HTMLElement[];
    expect(h1.innerHTML).toBe('Main');
    expect(h2.innerHTML).toBe('Section B');
    expect(h3.innerHTML).toBe(''); // no h3 persisted because decorative + cleared after depth 2
  });
});
