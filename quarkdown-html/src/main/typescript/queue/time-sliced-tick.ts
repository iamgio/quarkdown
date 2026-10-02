/**
 * Schedules a callback to run later, with the same contract as `requestAnimationFrame`.
 */
export type Tick = (callback: () => void) => void;

/** Default time, in milliseconds, that callbacks may run back-to-back before yielding to a frame. */
const DEFAULT_BUDGET_MS = 50;

/**
 * Options for {@link createTimeSlicedTick}.
 */
export interface TimeSlicedTickOptions {
    /** Time, in milliseconds, that callbacks may run back-to-back before yielding to a frame. */
    budgetMs?: number;
    /** Current time supplier, in milliseconds. */
    now?: () => number;
    /** Scheduler that runs a callback on the next frame, after rendering. */
    nextFrame?: Tick;
    /** Scheduler that runs a callback as soon as possible, in a new task. */
    nextTask?: Tick;
}

/**
 * Creates a tick that runs callbacks back-to-back in separate tasks for up to a time budget,
 * then yields to the next frame so that the browser can render, starting a new time slice.
 *
 * Compared to scheduling every callback on `requestAnimationFrame`, this avoids idling until the next frame
 * after each short unit of work, while still keeping the page responsive and progressively painted.
 *
 * @param options scheduling options
 * @returns the time-sliced tick
 */
export function createTimeSlicedTick({
    budgetMs = DEFAULT_BUDGET_MS,
    now = () => performance.now(),
    nextFrame = callback => requestAnimationFrame(() => callback()),
    nextTask = createMessageChannelTick(),
}: TimeSlicedTickOptions = {}): Tick {
    let sliceStart = now();

    return callback => {
        if (now() - sliceStart < budgetMs) {
            nextTask(callback);
            return;
        }
        nextFrame(() => {
            sliceStart = now();
            callback();
        });
    };
}

/**
 * Creates a tick that runs each callback in a new task via a `MessageChannel`,
 * which, unlike `setTimeout`, is not subject to timer clamping.
 * @returns the task-based tick
 */
function createMessageChannelTick(): Tick {
    const channel = new MessageChannel();
    const pending: (() => void)[] = [];
    channel.port1.onmessage = () => pending.shift()?.();

    return callback => {
        pending.push(callback);
        channel.port2.postMessage(null);
    };
}
