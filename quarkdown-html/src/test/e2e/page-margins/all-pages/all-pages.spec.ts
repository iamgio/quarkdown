import {suite} from "../../quarkdown";
import {DOCS_CONTAINERS, getPageContainers, MARGIN_NAMES, MARGIN_SUFFIX} from "../index";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "renders page margins correctly",
    ["paged", "slides", "slides-print", "docs"],
    async (page, docType) => {
        async function assertMarginContent(marginContent: ReturnType<typeof page.locator>, text: string) {
            await expect(marginContent).toBeAttached();
            const paragraph = marginContent.locator("> p");
            await expect(paragraph).toBeAttached();
            await expect(paragraph).toHaveText(text);
            await expect(marginContent.locator("> *")).toHaveCount(1);
        }

        // Docs: single instance per margin, no pagination
        if (docType === "docs") {
            for (const marginName of MARGIN_NAMES) {
                const suffix = MARGIN_SUFFIX[marginName];
                const containerSelector = DOCS_CONTAINERS[suffix];
                const container = page.locator(containerSelector);
                const marginContent = container.locator(`.page-margin-${suffix}.page-margin-content`);
                await assertMarginContent(marginContent, marginName);
            }
            return;
        }

        // Paged and slides: 3 pages/slides with margins on each
        const containers = getPageContainers(page, docType);
        await expect(containers).toHaveCount(3);

        for (const marginName of MARGIN_NAMES) {
            const suffix = MARGIN_SUFFIX[marginName];

            for (let i = 0; i < 3; i++) {
                const container = containers.nth(i);

                if (docType === "paged") {
                    const margin = container.locator(`.pagedjs_margin-${suffix}`);
                    await expect(margin).toHaveClass(/hasContent/);

                    const content = margin.locator("> .pagedjs_margin-content");
                    await expect(content).toBeAttached();
                    await expect(content).toHaveClass(/page-margin-content/);
                    await expect(margin.locator("> *")).toHaveCount(1);

                    await assertMarginContent(content, marginName);
                } else {
                    const marginContent = container.locator(`.page-margin-${suffix}.page-margin-content`);
                    await assertMarginContent(marginContent, marginName);
                }
            }
        }
    }
);
