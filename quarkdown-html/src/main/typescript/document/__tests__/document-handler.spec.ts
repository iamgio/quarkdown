import {describe, expect, it, vi} from 'vitest';
import {
    type ConditionalDocumentHandler,
    DocumentHandler,
    filterConditionalHandlers
} from '../../document/document-handler';

class TestDoc {}

class TestHandler extends DocumentHandler {
  public inited = false;
  constructor(doc: any) { super(doc as any); }
  init() { this.inited = true; }
  onPreRendering = vi.fn(async () => {})
  onPostRendering = vi.fn(async () => {})
}

// Minimal pre/post queues shim for testing pushToQueue
vi.mock('../../queue/execution-queues', () => {
  const pushed: any[] = [];
  const makeQueue = () => ({
    items: pushed,
    pushAsync: (fn: () => Promise<void>) => { pushed.push(fn); },
    execute: async () => { for (const fn of pushed) await fn(); pushed.length = 0; }
  });
  return {
    preRenderingExecutionQueue: makeQueue(),
    postRenderingExecutionQueue: makeQueue()
  };
});

describe('filterConditionalHandlers', () => {
  it('filters out booleans and keeps only DocumentHandler instances', () => {
    const doc = new TestDoc();
    const h1 = new TestHandler(doc);
    const arr: ConditionalDocumentHandler[] = [h1, true, false];
    const result = filterConditionalHandlers(arr);
    expect(result).toEqual([h1]);
  });
});

describe('DocumentHandler.pushToQueue', async () => {
  it('calls init and pushes pre/post rendering handlers if present', async () => {
    const doc = new TestDoc();
    const handler = new TestHandler(doc);

    // import queues after mock in same module scope
    const { preRenderingExecutionQueue, postRenderingExecutionQueue } = await import('../../queue/execution-queues');

    handler.pushToQueue();

    expect(handler.inited).toBe(true);

    // Execute queued functions to ensure they call the right hooks
    await preRenderingExecutionQueue.execute();
    await postRenderingExecutionQueue.execute();

    expect(handler.onPreRendering).toHaveBeenCalledTimes(1);
    expect(handler.onPostRendering).toHaveBeenCalledTimes(1);
  });
});
