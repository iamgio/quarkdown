import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("preserves an oversized unbreakable row and all following content", async (page) => {
    const pages = page.locator(".pagedjs_page");
    expect(await pages.count()).toBeGreaterThan(1);

    const rows = pages.locator("table > tbody > tr");
    for (const label of ["Row 1", "Row 2", "Row 4", "Row 5"]) {
        const row = rows.filter({has: page.locator("td", {hasText: new RegExp(`^${label}$`)})});
        await expect(row).toHaveCount(1);
        await expect(row).toHaveCSS("break-inside", "avoid");
    }

    for (const table of await pages.locator("table").all()) {
        await expect(table.locator(":scope > thead")).toHaveCount(1);
    }

    // Compare all cells with the unpaginated HTML, so no label or body text can disappear.
    const source = await page.request.get(page.url());
    const expected = await page.evaluate(html => {
        const document = new DOMParser().parseFromString(html, "text/html");
        return Array.from(document.querySelectorAll("table > tbody > tr > td"))
            .map(cell => cell.textContent).join("").replace(/\s/g, "");
    }, await source.text());
    const text = (await rows.locator("td").allTextContents()).join("").replace(/\s/g, "");
    expect(text).toBe(expected);
    const paragraph = pages.getByText("This paragraph after the table is missing too.", {exact: true});
    await expect(paragraph).toBeVisible();
    expect(await paragraph.evaluate(element => {
        const lastRow = Array.from(document.querySelectorAll(".pagedjs_page table > tbody > tr")).pop();
        return Boolean(lastRow!.compareDocumentPosition(element) & Node.DOCUMENT_POSITION_FOLLOWING);
    })).toBe(true);
});
