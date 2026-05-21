import {suite} from "../../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "shows colored body background and shadow around pages, only in screen view",
    ["paged"],
    async (page) => {
        const body = page.locator("body");
        const pagedPage = page.locator(".pagedjs_page").first();

        // Screen view: body has the lightgray background, pages have a drop shadow.
        const bodyBgScreen = await body.evaluate((el) => getComputedStyle(el).backgroundColor);
        const shadowScreen = await pagedPage.evaluate((el) => getComputedStyle(el).boxShadow);

        expect(bodyBgScreen).toBe("rgb(211, 211, 211)");
        expect(shadowScreen).not.toBe("none");

        // Print view: both are dropped to avoid bloating the PDF output.
        await page.emulateMedia({media: "print"});

        const bodyBgPrint = await body.evaluate((el) => getComputedStyle(el).backgroundColor);
        const shadowPrint = await pagedPage.evaluate((el) => getComputedStyle(el).boxShadow);

        expect(bodyBgPrint).not.toBe(bodyBgScreen);
        expect(shadowPrint).toBe("none");
    }
);
