import {suite} from "../../../quarkdown";
import {A4_HEIGHT_PX, getPageSizeTarget} from "../../index";

const {testMatrix, expect} = suite(__dirname);

const EXPECTED_WIDTH = 100;

testMatrix(
    "applies page width to correct element",
    ["plain", "paged", "slides", "docs"],
    async (page, docType) => {
        const target = getPageSizeTarget(page, docType);

        switch (docType) {
            case "paged":
                // In paged, unset height defaults to A4 height
                const box = await target.boundingBox();
                expect(box).not.toBeNull();
                expect(box!.width).toBeCloseTo(EXPECTED_WIDTH, 0);
                expect(box!.height).toBeCloseTo(A4_HEIGHT_PX, 0);
                break;
            case "plain":
            case "docs":
                for (let i = 0; i < 2; i++) {
                    const isPrint = i !== 0;

                    if (isPrint) {
                        await page.emulateMedia({media: "print"});
                    }

                    const targetBox = await target.boundingBox();
                    const body = page.locator("body");
                    const bodyBox = await body.boundingBox();
                    expect(bodyBox).not.toBeNull();
                    if (isPrint) {
                        expect(targetBox!.width).not.toBeCloseTo(EXPECTED_WIDTH, 0);
                        expect(targetBox!.width).toBeCloseTo(bodyBox!.width, -2);
                    } else {
                        expect(targetBox!.width).toBeCloseTo(EXPECTED_WIDTH, 0);
                        expect(targetBox!.width).not.toBeCloseTo(bodyBox!.width, -2);
                    }
                }
                break;
            case "slides":
                const slide = await target.boundingBox();
                expect(slide).not.toBeNull();
                expect(slide!.width).toBeCloseTo(EXPECTED_WIDTH, 0);
        }
    }
);
