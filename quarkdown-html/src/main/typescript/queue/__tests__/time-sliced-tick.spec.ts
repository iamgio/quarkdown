import {describe, expect, it} from 'vitest';
import {createTimeSlicedTick, Tick} from "../time-sliced-tick";

/**
 * A scheduler that records the callbacks it receives, running them only on demand.
 */
class ManualScheduler {
    readonly pending: (() => void)[] = [];
    readonly tick: Tick = callback => this.pending.push(callback);

    flush() {
        this.pending.splice(0).forEach(callback => callback());
    }
}

describe('createTimeSlicedTick', () => {
    function setup(budgetMs: number) {
        const clock = {now: 0};
        const frame = new ManualScheduler();
        const task = new ManualScheduler();
        const tick = createTimeSlicedTick({budgetMs, now: () => clock.now, nextFrame: frame.tick, nextTask: task.tick});
        return {clock, frame, task, tick};
    }

    it('schedules callbacks as tasks while within the budget', () => {
        const {clock, frame, task, tick} = setup(50);
        const calls: number[] = [];

        tick(() => calls.push(1));
        clock.now = 49;
        tick(() => calls.push(2));
        task.flush();

        expect(calls).toEqual([1, 2]);
        expect(frame.pending).toHaveLength(0);
    });

    it('yields to the next frame once the budget is exhausted', () => {
        const {clock, frame, task, tick} = setup(50);
        const calls: number[] = [];

        clock.now = 50;
        tick(() => calls.push(1));

        expect(task.pending).toHaveLength(0);
        expect(frame.pending).toHaveLength(1);
        frame.flush();
        expect(calls).toEqual([1]);
    });

    it('starts a new slice after yielding to a frame', () => {
        const {clock, frame, task, tick} = setup(50);

        clock.now = 60;
        tick(() => {});
        clock.now = 70;
        frame.flush();

        clock.now = 110;
        tick(() => {});
        expect(task.pending).toHaveLength(1);
        expect(frame.pending).toHaveLength(0);
    });
});
