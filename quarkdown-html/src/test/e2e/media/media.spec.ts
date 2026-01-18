import {existsSync} from "fs";
import {join} from "path";
import {outputDir, suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("renders image from media storage", async (page) => {
    const img = page.locator("img");
    await expect(img).toBeAttached();

    // Image source should be transformed to media/rect@HASH.png
    const src = await img.getAttribute("src");
    expect(src).toMatch(/^media\/rect@[-\d]+\.png$/);

    // Media file should exist on disk
    const mediaPath = join(outputDir(__dirname), src!);
    expect(existsSync(mediaPath)).toBe(true);

    // Image should be visible and have non-zero dimensions
    await expect(img).toBeVisible();
    const box = await img.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.width).toBeGreaterThan(0);
    expect(box!.height).toBeGreaterThan(0);
});
