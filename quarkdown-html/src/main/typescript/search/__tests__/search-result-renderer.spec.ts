import {describe, expect, it} from "vitest";
import {renderResultItem, renderResultItems} from "../search-result-renderer";
import {DisplayItem} from "../search-result-expander";

describe("renderResultItem", () => {
    it("renders basic result item", () => {
        const item: DisplayItem = {
            url: "/docs/page",
            title: "Page Title",
            description: "Description text",
        };
        const html = renderResultItem(item, 0);

        expect(html).toContain('href="/docs/page"');
        expect(html).toContain('class="search-result"');
        expect(html).toContain("Page Title");
        expect(html).toContain("Description text");
        expect(html).toContain('data-index="0"');
        expect(html).toContain('role="option"');
    });

    it("renders heading result with parent title and chevron", () => {
        const item: DisplayItem = {
            url: "/docs/page#section",
            title: "Section Title",
            description: "",
            parentTitle: "Page Title",
        };
        const html = renderResultItem(item, 1);

        expect(html).toContain('class="search-result search-result-heading"');
        expect(html).toContain("Page Title");
        expect(html).toContain('class="search-result-chevron"');
        expect(html).toContain("Section Title");
        expect(html).toContain('data-index="1"');
    });

    it("omits description div when description is empty", () => {
        const item: DisplayItem = {
            url: "/docs/page",
            title: "Page Title",
            description: "",
        };
        const html = renderResultItem(item, 0);

        expect(html).not.toContain("search-result-description");
    });

    it("includes description div when description has content", () => {
        const item: DisplayItem = {
            url: "/docs/page",
            title: "Page Title",
            description: "Some description",
        };
        const html = renderResultItem(item, 0);

        expect(html).toContain('class="search-result-description"');
        expect(html).toContain("Some description");
    });

    it("escapes HTML in title", () => {
        const item: DisplayItem = {
            url: "/docs/page",
            title: "<script>alert('xss')</script>",
            description: "",
        };
        const html = renderResultItem(item, 0);

        expect(html).toContain("&lt;script&gt;");
        expect(html).not.toContain("<script>alert");
    });

    it("escapes HTML in URL", () => {
        const item: DisplayItem = {
            url: "/docs/page?q=<script>",
            title: "Page",
            description: "",
        };
        const html = renderResultItem(item, 0);

        expect(html).toContain("&lt;script&gt;");
        expect(html).not.toContain('href="/docs/page?q=<script>"');
    });

    it("escapes HTML in parent title", () => {
        const item: DisplayItem = {
            url: "/docs/page#section",
            title: "Section",
            description: "",
            parentTitle: "<b>Parent</b>",
        };
        const html = renderResultItem(item, 0);

        expect(html).toContain("&lt;b&gt;Parent&lt;/b&gt;");
    });
});

describe("renderResultItems", () => {
    it("renders empty string for empty array", () => {
        expect(renderResultItems([])).toBe("");
    });

    it("renders multiple items with correct indices", () => {
        const items: DisplayItem[] = [
            {url: "/a", title: "A", description: ""},
            {url: "/b", title: "B", description: ""},
            {url: "/c", title: "C", description: ""},
        ];
        const html = renderResultItems(items);

        expect(html).toContain('data-index="0"');
        expect(html).toContain('data-index="1"');
        expect(html).toContain('data-index="2"');
    });

    it("concatenates all item HTML", () => {
        const items: DisplayItem[] = [
            {url: "/first", title: "First", description: ""},
            {url: "/second", title: "Second", description: "Desc"},
        ];
        const html = renderResultItems(items);

        expect(html).toContain("First");
        expect(html).toContain("Second");
        expect(html).toContain("Desc");
    });
});
