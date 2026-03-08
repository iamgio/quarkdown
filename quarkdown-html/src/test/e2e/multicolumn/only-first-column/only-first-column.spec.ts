import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "2-column paragraph fills only the first column",
    ["plain", "paged", "slides", "docs"],
    async (page) => {
        const paragraph = page.locator("p").first();
        await expect(paragraph).toBeVisible();

        const paragraphBox = await paragraph.boundingBox();
        expect(paragraphBox).not.toBeNull();

        // The paragraph should occupy roughly half the available width, since it fills only the first of two columns.
        const viewportWidth = page.viewportSize()!.width;
        expect(paragraphBox!.width).toBeLessThan(viewportWidth * 0.6);
    },
);
