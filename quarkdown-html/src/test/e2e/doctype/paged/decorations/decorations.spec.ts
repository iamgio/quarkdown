import {suite} from "../../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const LIGHTGRAY = "rgb(211, 211, 211)";

testMatrix(
    "shows colored body background and shadow around pages, only in screen view",
    ["paged"],
    async (page) => {
        const body = page.locator("body");
        const pagedPage = page.locator(".pagedjs_page").first();

        await expect(pagedPage).toBeAttached();

        // Screen view: body has the lightgray background, pages have a drop shadow.
        await expect(body).toHaveCSS("background-color", LIGHTGRAY);
        await expect(pagedPage).not.toHaveCSS("box-shadow", "none");

        // Print view: both are dropped to avoid bloating the PDF output.
        await page.emulateMedia({media: "print"});

        await expect(body).not.toHaveCSS("background-color", LIGHTGRAY);
        await expect(pagedPage).toHaveCSS("box-shadow", "none");
    }
);
