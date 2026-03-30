import {Page} from "@playwright/test";

/**
 * Retrieves the Reveal.js transition configuration from the page.
 */
export async function getTransitionConfig(page: Page) {
    return page.evaluate(() => {
        const config = (window as any).Reveal.getConfig();
        return {transition: config.transition as string, transitionSpeed: config.transitionSpeed as string};
    });
}
