import {suite} from "../../../quarkdown";
import {getTransitionConfig} from "../transition-config";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "uses default slide transition",
    ["slides"],
    async (page) => {
        const config = await getTransitionConfig(page);
        expect(config.transition).toBe("slide");
        expect(config.transitionSpeed).toBe("default");

        // Navigate and verify the slide changes
        await expect(page.locator(".reveal .slides > section.present h1")).toHaveText("Slide 1");
        await page.keyboard.press("ArrowRight");
        await expect(page.locator(".reveal .slides > section.present h1")).toHaveText("Slide 2");
    }
);
