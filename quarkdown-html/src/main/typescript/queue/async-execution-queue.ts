/**
 * A queue for managing and executing asynchronous functions in parallel.
 *
 * This class allows you to collect multiple asynchronous functions and execute them
 * all at once using Promise.all(). It provides a way to track completion status
 * and execute a callback after all queued functions have completed.
 *
 * @example
 * ```typescript
 * const queue = new AsyncExecutionQueue(() => console.log('All done!'));
 *
 * queue.push(async () => { await ... });
 * queue.push(async () => { await ... });
 *
 * await queue.execute(); // Executes all functions in parallel
 * console.log(queue.isCompleted()); // true
 * ```
 */
export class AsyncExecutionQueue {
    /** Array of async functions waiting to be executed */
    private queue: Array<() => Promise<void>> = [];

    /** Callback function executed after all queued functions complete */
    private onComplete: Array<() => void> = [];

    /** Flag indicating whether the queue has been executed and completed */
    private completed: boolean = false;

    /**
     * Adds an asynchronous function to the execution queue.
     *
     * @param fn - An async function that returns a Promise<void> to be executed later
     */
    pushAsync(fn: () => Promise<void>) {
        this.queue.push(fn);
    }

    /**
     * Adds a synchronous function to the execution queue.
     *
     * This method wraps the provided synchronous function in an async function
     * that returns a resolved Promise, allowing it to be executed in the same
     * manner as other async functions in the queue.
     *
     * @param fn - A synchronous function to be executed later
     */
    push(fn: () => void) {
        this.queue.push(async () => fn());
    }

    /**
     * Registers a callback to be called after all queued functions have executed.
     *
     * @param fn - A function to be called once after `execute()` completes
     */
    addOnComplete(fn: () => void) {
        this.onComplete.push(fn);
    }

    /**
     * Executes all queued functions in parallel and clears the queue.
     *
     * This method uses Promise.all() to run all queued functions concurrently,
     * waits for all of them to complete, then clears the queue and calls the
     * onExecute callback. After execution, the queue is marked as completed.
     *
     * @returns A Promise that resolves when all queued functions have completed
     */
    async execute(): Promise<void> {
        await Promise.all(this.queue.map(async fn => fn()));
        this.queue = [];
        this.onComplete?.forEach(fn => fn());
        this.completed = true;
    }

    /**
     * Checks whether the queue has been executed and completed.
     *
     * @returns true if `execute()` has been called and completed, false otherwise
     */
    isCompleted(): boolean {
        return this.completed;
    }
}