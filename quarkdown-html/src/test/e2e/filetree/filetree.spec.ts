import {getBeforeContent} from "../__util/css";
import {suite} from "../quarkdown";

const {test, expect} = suite(__dirname);

test("list has no unexpected margin or padding", async (page) => {
    const rootList = page.locator(".file-tree > ul");
    await expect(rootList).toHaveCount(1);

    // Root ul should have controlled margin and padding from _filetree.scss,
    // not polluted by global list styles.
    const rootStyle = await rootList.evaluate((el) => {
        const s = getComputedStyle(el);
        return {
            listStyle: s.listStyleType,
            marginLeft: parseFloat(s.marginLeft),
            paddingLeft: parseFloat(s.paddingLeft),
        };
    });
    expect(rootStyle.listStyle).toBe("none");
    
    expect(rootStyle.marginLeft).toBeLessThanOrEqual(16);
    expect(rootStyle.paddingLeft).toBeLessThanOrEqual(16);

    // Nested ul should also have no list style, controlled padding, and a left border.
    const nestedUl = page.locator(".file-tree ul ul");
    await expect(nestedUl).toHaveCount(1);
    const nestedStyle = await nestedUl.evaluate((el) => {
        const s = getComputedStyle(el);
        return {
            listStyle: s.listStyleType,
            paddingLeft: parseFloat(s.paddingLeft),
            borderLeftStyle: s.borderLeftStyle,
            borderLeftWidth: parseFloat(s.borderLeftWidth),
        };
    });
    expect(nestedStyle.listStyle).toBe("none");
    expect(nestedStyle.paddingLeft).toBeLessThanOrEqual(20);
    expect(nestedStyle.borderLeftStyle).toBe("solid");
    expect(nestedStyle.borderLeftWidth).toBeGreaterThan(0);
});

test("icons are present on all entries", async (page) => {
    const files = page.locator(".file-tree li.file");
    const directories = page.locator(".file-tree li.directory");
    const ellipsis = page.locator(".file-tree li.ellipsis");

    await expect(files).toHaveCount(3); // main.ts, utils.ts, README.md
    await expect(directories).toHaveCount(1); // src
    await expect(ellipsis).toHaveCount(1);

    // Each file entry has a ::before icon.
    for (const file of await files.all()) {
        const content = await getBeforeContent(file);
        expect(content).not.toBe("none");
        expect(content).not.toBe('""');
    }

    // Directory entry has a ::before icon.
    const dirContent = await getBeforeContent(directories.first());
    expect(dirContent).not.toBe("none");
    expect(dirContent).not.toBe('""');

    // Ellipsis entry has a ::before icon.
    const ellipsisContent = await getBeforeContent(ellipsis.first());
    expect(ellipsisContent).not.toBe("none");
    expect(ellipsisContent).not.toBe('""');
});

test("highlighted entries have background and fit-content width", async (page) => {
    const highlighted = page.locator(".file-tree li[data-highlighted]");
    await expect(highlighted).toHaveCount(2); // main.ts, README.md

    const highlightColor = await page.evaluate(() =>
        getComputedStyle(document.documentElement).getPropertyValue("--qd-file-tree-highlight-color").trim()
    );
    // The variable should be defined.
    expect(highlightColor).toBeTruthy();

    for (const entry of await highlighted.all()) {
        // Background color should be applied (not transparent/empty).
        const bg = await entry.evaluate((el) => getComputedStyle(el).backgroundColor);
        expect(bg).not.toBe("rgba(0, 0, 0, 0)");

        // Width should be fit-content, meaning the element is narrower than its parent.
        const {entryWidth, parentWidth} = await entry.evaluate((el) => ({
            entryWidth: el.getBoundingClientRect().width,
            parentWidth: el.parentElement!.getBoundingClientRect().width,
        }));
        expect(entryWidth).toBeLessThan(parentWidth);
    }

    // Non-highlighted entries should not have a background.
    const nonHighlighted = page.locator(".file-tree li:not([data-highlighted])");
    for (const entry of await nonHighlighted.all()) {
        const bg = await entry.evaluate((el) => getComputedStyle(el).backgroundColor);
        expect(bg).toBe("rgba(0, 0, 0, 0)");
    }
});
