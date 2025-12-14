import {describe, expect, it} from 'vitest';
import {PageMarginsDocumentHandler} from "../page-margins/page-margins-document-handler";
import {PagedLikeQuarkdownDocument, QuarkdownPage} from "../../paged-like-quarkdown-document";

class DummyDocument implements PagedLikeQuarkdownDocument<QuarkdownPage> {
  constructor(private readonly pages: QuarkdownPage[] = [], private readonly types: Array<'left'|'right'> = []) {
  }

  getPages(): QuarkdownPage[] {
    return this.pages;
  }

  getPageNumber(_page: QuarkdownPage, _includeDisplayNumbers?: boolean): number {
    return 1;
  }

  getPageType(page: QuarkdownPage): 'left' | 'right' {
    const index = this.pages.indexOf(page);
    return this.types[index] ?? 'right';
  }

  getPage(_element: HTMLElement): QuarkdownPage | undefined {
    return undefined;
  }

  setDisplayPageNumber(_page: QuarkdownPage, _pageNumber: number): void {
  }

  getParentViewport(_element: Element): HTMLElement | undefined {
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

const createInitializer = (className: string, leftPosition: string, rightPosition: string): HTMLElement => {
  const div = document.createElement('div');
  div.className = className;
  div.dataset.onLeftPage = leftPosition;
  div.dataset.onRightPage = rightPosition;
  return div;
};

const createPage = (initializers: HTMLElement[] = []): QuarkdownPage => {
  const container = document.createElement('div');
  initializers.forEach(init => container.appendChild(init));
  return {
    querySelectorAll(selector: string) {
      return container.querySelectorAll(selector);
    },
  } as QuarkdownPage;
};

describe('PageMarginsDocumentHandler', () => {
  it('hides page margin initializers on pre-rendering', async () => {
    document.body.innerHTML = `
    <div>
    <div class="page-margin-content" id="a" data-on-left-page="bottom-center" data-on-right-page="bottom-center"></div>
    <div class="page-margin-content" id="b" data-on-left-page="top-left" data-on-right-page="top-left"></div>
    <div class="other"></div>
    </div>`;

    const handler = new RecordingHandler(new DummyDocument());
    await handler.onPreRendering();

    const initializers = document.querySelectorAll<HTMLElement>('.page-margin-content');
    expect(initializers.length).toBe(2);
    initializers.forEach(init => {
      expect(init.style.display).toBe('none');
    });
  });

  it('applies page margins starting from the page where they are defined', async () => {
    // Margin defined on page 1 should appear on pages 1, 2, and 3.
    const marginOnPage1 = createInitializer('page-margin-content margin-a', 'bottom-center', 'bottom-center');
    // Margin defined on page 2 should appear on pages 2 and 3.
    const marginOnPage2 = createInitializer('page-margin-content margin-b', 'top-left', 'top-left');

    const pages = [
      createPage([marginOnPage1]),
      createPage([marginOnPage2]),
      createPage([]),
    ];
    const doc = new DummyDocument(pages, ['right', 'left', 'right']);
    const handler = new RecordingHandler(doc);

    await handler.onPreRendering();
    await handler.onPostRendering();

    expect(handler.applied).toEqual([
      // Page 0: only margin-a is active
      {pageIndex: 0, marginId: undefined, marginPosition: 'bottom-center'},
      // Page 1: margin-a persists, margin-b starts
      {pageIndex: 1, marginId: undefined, marginPosition: 'bottom-center'},
      {pageIndex: 1, marginId: undefined, marginPosition: 'top-left'},
      // Page 2: both margins persist
      {pageIndex: 2, marginId: undefined, marginPosition: 'bottom-center'},
      {pageIndex: 2, marginId: undefined, marginPosition: 'top-left'},
    ]);
  });

  it('uses correct position based on page type (left/right)', async () => {
    // Margin with different positions for left and right pages.
    const margin = createInitializer('page-margin-content margin-mirror', 'left-center', 'right-center');

    const pages = [
      createPage([margin]),
      createPage([]),
    ];
    const doc = new DummyDocument(pages, ['right', 'left']);
    const handler = new RecordingHandler(doc);

    await handler.onPreRendering();
    await handler.onPostRendering();

    expect(handler.applied).toEqual([
      {pageIndex: 0, marginId: undefined, marginPosition: 'right-center'},
      {pageIndex: 1, marginId: undefined, marginPosition: 'left-center'},
    ]);
  });

  it('overrides previous margin when a new one with the same class is defined', async () => {
    // Two margins with the same class on different pages.
    const marginV1 = createInitializer('page-margin-content footer', 'bottom-center', 'bottom-center');
    const marginV2 = createInitializer('page-margin-content footer', 'top-center', 'top-center');

    const pages = [
      createPage([marginV1]),
      createPage([marginV2]),
      createPage([]),
    ];
    const doc = new DummyDocument(pages, ['right', 'right', 'right']);
    const handler = new RecordingHandler(doc);

    await handler.onPreRendering();
    await handler.onPostRendering();

    expect(handler.applied).toEqual([
      // Page 0: uses marginV1
      {pageIndex: 0, marginId: undefined, marginPosition: 'bottom-center'},
      // Page 1: marginV2 overrides marginV1
      {pageIndex: 1, marginId: undefined, marginPosition: 'top-center'},
      // Page 2: marginV2 persists
      {pageIndex: 2, marginId: undefined, marginPosition: 'top-center'},
    ]);
  });
});
