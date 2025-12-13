import {describe, expect, it} from 'vitest';
import {PageMarginsDocumentHandler} from "../page-margins/page-margins-document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../../paged-like-quarkdown-document";

class DummyDocument implements PagedLikeQuarkdownDocument<QuarkdownPage> {
  constructor(private readonly pages: QuarkdownPage[] = [], private readonly types: Array<'left'|'right'> = []) {
  }

  getPages(): QuarkdownPage[] {
    return this.pages;
  }

  getPageNumber(): number {
    return 1;
  }

  getPageType(page: QuarkdownPage): 'left' | 'right' {
    const index = this.pages.indexOf(page);
    return this.types[index] ?? 'right';
  }

  getParentViewport(): HTMLElement | undefined {
    return undefined;
  }

  setupPreRenderingHook(): void {
  }

  setupPostRenderingHook(): void {
  }

  initializeRendering(): void {
  }

  getHandlers() {
    return [];
  }
}

class RecordingHandler extends PageMarginsDocumentHandler {
  public readonly applied: Array<{pageIndex: number, marginId: string | undefined, marginPosition: string}> = [];

  apply(initializer: HTMLElement, page: QuarkdownPage, marginPositionName: string) {
    this.applied.push({
      pageIndex: (this.quarkdownDocument.getPages().indexOf(page)),
      marginId: initializer.dataset.marginId,
      marginPosition: marginPositionName,
    });
  }
}

const createSwitch = (id: string) => {
  const span = document.createElement('span');
  span.className = 'page-margin-switch';
  span.dataset.marginId = id;
  return span;
};

const createPage = (switches: string[] = []): QuarkdownPage => {
  const container = document.createElement('div');
  switches.forEach(id => container.appendChild(createSwitch(id)));
  return {
    querySelectorAll(selector: string) {
      return container.querySelectorAll(selector);
    },
  } as QuarkdownPage;
};

describe('PageMarginsDocumentHandler', () => {
  it('collects and removes page margin initializers on pre-rendering', async () => {
    document.body.innerHTML = `
    <div>
    <div class="page-margin-content" id="a" data-margin-id="1" data-margin-position="bottom-center"></div>
    <div class="page-margin-content" id="b" data-margin-id="2" data-margin-position="bottom-center"></div>
    <div class="other"></div>
    </div>`;

    const handler = new RecordingHandler(new DummyDocument());
    await handler.onPreRendering();

    // @ts-expect-error access for testing
    const collected = handler.pageMarginInitializers;
    expect(Array.from(collected.keys()).sort()).toEqual(['1', '2']);
    expect(document.querySelectorAll('.page-margin-content').length).toBe(0);
  });

  it('applies page margins based on page switches', async () => {
    document.body.innerHTML = `
    <div>
    <div class="page-margin-content" data-margin-id="m1" data-margin-position="bottom-center" data-on-left-page="bottom-center" data-on-right-page="bottom-center">A</div>
    <span class="page-margin-switch" data-margin-id="m1"></span>
    <div class="page-margin-content" data-margin-id="m2" data-margin-position="bottom-center" data-on-left-page="bottom-center" data-on-right-page="bottom-center">B</div>
    <span class="page-margin-switch" data-margin-id="m2"></span>
    </div>`;

    const pages = [createPage(['m1']), createPage(['m2'])];
    const doc = new DummyDocument(pages, ['right', 'right']);
    const handler = new RecordingHandler(doc);

    await handler.onPreRendering();
    await handler.onPostRendering();

    expect(handler.applied).toEqual([
      {pageIndex: 0, marginId: 'm1', marginPosition: 'bottom-center'},
      {pageIndex: 1, marginId: 'm2', marginPosition: 'bottom-center'},
    ]);
  });
});
