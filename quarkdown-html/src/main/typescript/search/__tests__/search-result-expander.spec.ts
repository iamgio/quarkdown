import {describe, expect, it} from "vitest";
import {expandResult} from "../search-result-expander";
import {DocumentSearchResult} from "../search";

function createResult(overrides: Partial<DocumentSearchResult> = {}): DocumentSearchResult {
    return {
        entry: {
            url: "/docs/page",
            title: "Page Title",
            description: null,
            keywords: [],
            content: "Page content here",
            headings: [],
        },
        score: 10,
        matchedTerms: ["term"],
        matchedFields: {term: ["content"]},
        ...overrides,
    };
}

describe("expandResult", () => {
    it("returns main page result", () => {
        const result = createResult();
        const items = expandResult(result);

        expect(items).toHaveLength(1);
        expect(items[0].url).toBe("/docs/page");
        expect(items[0].title).toBe("Page Title");
        expect(items[0].parentTitle).toBeUndefined();
    });

    it("uses url as title when title is null", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: null,
                description: null,
                keywords: [],
                content: "Content",
                headings: [],
            },
        });
        const items = expandResult(result);

        expect(items[0].title).toBe("/docs/page");
    });

    it("adds heading results when headings field matched", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content",
                headings: [
                    {anchor: "intro", text: "Introduction", level: 2},
                    {anchor: "setup", text: "Setup Guide", level: 2},
                ],
            },
            matchedTerms: ["intro"],
            matchedFields: {intro: ["headings"]},
        });
        const items = expandResult(result);

        expect(items).toHaveLength(2);
        expect(items[0].url).toBe("/docs/page");
        expect(items[1].url).toBe("/docs/page#intro");
        expect(items[1].title).toBe("Introduction");
        expect(items[1].parentTitle).toBe("Page Title");
    });

    it("adds multiple heading results for multiple matches", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content",
                headings: [
                    {anchor: "intro", text: "Introduction", level: 2},
                    {anchor: "setup", text: "Setup Guide", level: 2},
                    {anchor: "config", text: "Configuration", level: 3},
                ],
            },
            matchedTerms: ["intro", "config"],
            matchedFields: {intro: ["headings"], config: ["headings"]},
        });
        const items = expandResult(result);

        expect(items).toHaveLength(3);
        expect(items[1].url).toBe("/docs/page#intro");
        expect(items[2].url).toBe("/docs/page#config");
    });

    it("uses description for preview when available", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: "This is the description",
                keywords: [],
                content: "This is the content",
                headings: [],
            },
            matchedTerms: ["description"],
            matchedFields: {description: ["description"]},
        });
        const items = expandResult(result);

        expect(items[0].description).toContain("description");
    });

    it("does not highlight description when match is from title", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content with title word",
                headings: [],
            },
            matchedTerms: ["title"],
            matchedFields: {title: ["title"]},
        });
        const items = expandResult(result);

        expect(items[0].description).not.toContain("<strong>");
    });

    it("does not highlight description when match is from headings", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content with intro word",
                headings: [{anchor: "intro", text: "Introduction", level: 2}],
            },
            matchedTerms: ["intro"],
            matchedFields: {intro: ["headings"]},
        });
        const items = expandResult(result);

        expect(items[0].description).not.toContain("<strong>");
    });

    it("highlights description when match is from content", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content with searchterm here",
                headings: [],
            },
            matchedTerms: ["searchterm"],
            matchedFields: {searchterm: ["content"]},
        });
        const items = expandResult(result);

        expect(items[0].description).toContain("<strong>searchterm</strong>");
    });

    it("escapes HTML in descriptions", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "<div>some content</div>",
                headings: [],
            },
            matchedTerms: ["content"],
            matchedFields: {content: ["content"]},
        });
        const items = expandResult(result);

        expect(items[0].description).toContain("&lt;div&gt;");
        expect(items[0].description).not.toContain("<div>");
    });

    it("heading results have empty description", () => {
        const result = createResult({
            entry: {
                url: "/docs/page",
                title: "Page Title",
                description: null,
                keywords: [],
                content: "Content",
                headings: [{anchor: "intro", text: "Introduction", level: 2}],
            },
            matchedTerms: ["intro"],
            matchedFields: {intro: ["headings"]},
        });
        const items = expandResult(result);

        expect(items[1].description).toBe("");
    });
});
