import {DocumentHandler} from "../document-handler";

/**
 * Handler that adapts keybinding labels for macOS platforms.
 * On Mac, each `<kbd>` element inside a `.keybinding` container
 * swaps its displayed text with the value from its `data-mac` attribute,
 * showing platform-appropriate key names (e.g. "⌘" instead of "Ctrl").
 */
export class PlatformAwareKeybindings extends DocumentHandler {
    async onPreRendering() {
        const keybindings = document.querySelectorAll<HTMLElement>('.keybinding kbd');
        if (keybindings.length === 0) return;

        const isMac = (navigator as any).userAgentData?.platform === "macOS" || /Mac/.test(navigator.userAgent);
        if (!isMac) return;

        keybindings.forEach((kbd) => {
            const macName = kbd.dataset.mac;
            if (macName) {
                kbd.textContent = macName;
            }
        });
    }
}
