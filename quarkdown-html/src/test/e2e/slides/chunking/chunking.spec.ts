import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("chunks slides correctly", async (page) => {
    const slides = page.locator(".reveal .slides > section");
    await expect(slides).toHaveCount(5);

    // Slide 1: # A, ## B
    await expect(slides.nth(0).locator("h1")).toHaveText("A");
    await expect(slides.nth(0).locator("h2")).toHaveText("B");

    // Slide 2: # C
    await expect(slides.nth(1).locator("h1")).toHaveText("C");

    // Slide 3: # D
    await expect(slides.nth(2).locator("h1")).toHaveText("D");

    // Slide 4: explicit page break (<<<), then "xyz"
    await expect(slides.nth(3).locator("p")).toHaveText("xyz");

    // Slide 5: # E
    await expect(slides.nth(4).locator("h1")).toHaveText("E");
});
