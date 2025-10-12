const MESSAGE_SOURCE = 'quarkdown';
const TARGET_ORIGIN = '*';

/**
 * Notify the parent window (if exists) about an event in the live preview.
 * This is used to communicate with the Quarkdown editor.
 * @param event The event name
 * @param data Additional data to send with the event
 */
export function notifyLivePreview(event: string, data: Record<string, any> = {}) {
    if (!window.parent || window.parent === window) return;
    try {
        window.parent.postMessage(
            {
                source: MESSAGE_SOURCE,
                event,
                data,
                timestamp: Date.now()
            },
            TARGET_ORIGIN,
        );
    } catch (e) {
        console.error('Failed to post message to parent', e);
    }
}