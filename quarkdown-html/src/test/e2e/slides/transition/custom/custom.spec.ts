import {suite} from "../../../quarkdown";
import {getTransitionConfig} from "../transition-config";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "applies custom transition configuration",
    ["slides"],
    async (page) => {
        const config = await getTransitionConfig(page);
        expect(config.transition).toBe("fade");
        expect(config.transitionSpeed).toBe("fast");

        // Navigate and verify the slide changes with custom transition
        await expect(page.locator(".reveal .slides > section.present h1")).toHaveText("Slide 1");
        await page.keyboard.press("ArrowRight");
        await expect(page.locator(".reveal .slides > section.present h1")).toHaveText("Slide 2");
    }
);
