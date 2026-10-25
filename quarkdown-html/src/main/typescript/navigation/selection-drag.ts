/** Class set on an element while the pointer drags a selection that started outside it. */
export const SELECTING_CLASS = 'selecting';

/**
 * Marks the element with {@link SELECTING_CLASS} from a pointer press outside it until the pointer is released,
 * so that styles can keep it from reacting to the pointer, such as expanding on hover,
 * while the user drags to select content.
 * @param element - The element to mark, typically a floating navigation overlay
 */
export function markWhileSelectingOutside(element: HTMLElement): void {
    document.addEventListener('pointerdown', event => {
        if (event.target instanceof Node && element.contains(event.target)) return;
        element.classList.add(SELECTING_CLASS);
    });
    const release = () => element.classList.remove(SELECTING_CLASS);
    document.addEventListener('pointerup', release);
    document.addEventListener('pointercancel', release);
    window.addEventListener('blur', release);
}
