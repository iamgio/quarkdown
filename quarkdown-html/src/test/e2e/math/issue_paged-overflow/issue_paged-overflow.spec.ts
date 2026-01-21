import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("math does not overflow to next page incorrectly", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(2);

    const firstPage = pages.nth(0);
    const secondPage = pages.nth(1);

    // First page contains first math and container
    const firstPageMath = firstPage.locator("formula");
    const firstPageContainer = firstPage.locator(".container");
    await expect(firstPageMath).toHaveCount(1);
    await expect(firstPageContainer).toHaveCount(1);

    // Second page contains second math
    const secondPageMath = secondPage.locator("formula");
    const secondPageContainer = secondPage.locator(".container");
    await expect(secondPageMath).toHaveCount(1);
    await expect(secondPageContainer).toHaveCount(0);
});
