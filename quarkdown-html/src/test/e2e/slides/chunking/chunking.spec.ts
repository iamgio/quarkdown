import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("chunks slides correctly", async (page) => {
    const slides = page.locator(".reveal .slides > section");
    await expect(slides).toHaveCount(6);

    // Slide 1: # A
    await expect(slides.nth(0).locator("h1")).toHaveText("A");

    // Slide 2: ## B, split by the slides' default auto page break (up to depth 2)
    await expect(slides.nth(1).locator("h2")).toHaveText("B");

    // Slide 3: # C
    await expect(slides.nth(2).locator("h1")).toHaveText("C");

    // Slide 4: # D
    await expect(slides.nth(3).locator("h1")).toHaveText("D");

    // Slide 5: explicit page break (<<<), then "xyz"
    await expect(slides.nth(4).locator("p")).toHaveText("xyz");

    // Slide 6: # E
    await expect(slides.nth(5).locator("h1")).toHaveText("E");
});
