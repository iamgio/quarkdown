import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

const FOOTNOTE_COUNT = 6;

testMatrix(
    "places each footnote on the page of its reference",
    ["paged"],
    async (page) => {
        const definitions = page.locator(".pagedjs_footnote_area .footnote-definition");
        await expect(definitions).toHaveCount(FOOTNOTE_COUNT);

        const placements = await page.locator(".footnote-reference").evaluateAll(references =>
            references.map(reference => {
                const definitionId = (reference as HTMLElement).dataset.definition!;
                const definition = document.getElementById(definitionId)!;
                return {
                    referencePage: reference.closest(".pagedjs_page")?.id,
                    definitionPage: definition.closest(".pagedjs_page")?.id,
                };
            }),
        );

        expect(new Set(placements.map(p => p.referencePage)).size).toBeGreaterThan(1);
        placements.forEach(({referencePage, definitionPage}) => {
            expect(definitionPage).toBeDefined();
            expect(definitionPage).toBe(referencePage);
        });

        // The space reserved before pagination is released.
        await expect(page.locator(".footnote-reference").first()).toHaveCSS("display", "inline");
    },
);
