// Queue of actions to be executed before the document is handled by Reveal/Paged.
// The document is elaborated only after this queue is executed.
import {AsyncExecutionQueue} from "./async-execution-queue";

// Queue of actions to be executed before the document is handled by the document type's specific framework.
export const preRenderingExecutionQueue = new AsyncExecutionQueue();

// Queue of actions to be executed after the document has been rendered in its final form.
export const postRenderingExecutionQueue = new AsyncExecutionQueue();