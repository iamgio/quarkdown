import {describe, expect, it} from "vitest";
import {createSearch, DocumentSearch, SearchIndex} from "../search";

const testIndex: SearchIndex = {
    entries: [
        {
            url: "/",
            title: "Home",
            description: "Welcome to the documentation",
            keywords: ["home", "welcome"],
            content: "This is the home page with some content.",
            headings: [],
        },
        {
            url: "/getting-started",
            title: "Getting Started",
            description: "Learn how to get started",
            keywords: ["tutorial", "beginner"],
            content: "Follow these steps to begin using the application.",
            headings: [
                { anchor: "installation", text: "Installation", level: 1 },
                { anchor: "configuration", text: "Configuration", level: 2 },
            ],
        },
        {
            url: "/api",
            title: "API Reference",
            description: "Complete API documentation",
            keywords: ["api", "reference", "methods"],
            content: "The API provides methods for interacting with the system.",
            headings: [
                { anchor: "methods", text: "Methods", level: 1 },
                { anchor: "parameters", text: "Parameters", level: 2 },
            ],
        },
        {
            url: "/faq",
            title: "FAQ",
            description: null,
            keywords: [],
            content: "Frequently asked questions about the application.",
            headings: [],
        },
    ],
};

describe("DocumentSearch", () => {
    it("indexes entries correctly", () => {
        const search = new DocumentSearch(testIndex);
        expect(search.entryCount).toBe(4);
    });

    it("returns empty array for empty query", () => {
        const search = new DocumentSearch(testIndex);
        expect(search.search("")).toEqual([]);
        expect(search.search("   ")).toEqual([]);
    });

    it("finds exact matches", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("API");

        expect(results.length).toBeGreaterThan(0);
        expect(results[0].entry.url).toBe("/api");
        expect(results[0].matchedTerms).toContain("api");
    });

    it("performs fuzzy matching", () => {
        const search = new DocumentSearch(testIndex);
        // "instalation" (misspelled) should still find "Installation"
        const results = search.search("instalation");

        expect(results.length).toBeGreaterThan(0);
        expect(results.some((r) => r.entry.url === "/getting-started")).toBe(true);
    });

    it("performs prefix matching", () => {
        const search = new DocumentSearch(testIndex);
        // "config" should match "Configuration"
        const results = search.search("config");

        expect(results.length).toBeGreaterThan(0);
        expect(results.some((r) => r.entry.url === "/getting-started")).toBe(true);
    });

    it("boosts title matches higher than content", () => {
        const search = new DocumentSearch(testIndex);
        // "FAQ" appears in title of /faq and as "frequently asked questions" in content
        const results = search.search("FAQ");

        expect(results.length).toBeGreaterThan(0);
        expect(results[0].entry.url).toBe("/faq");
    });

    it("searches in headings", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("Parameters");

        expect(results.length).toBeGreaterThan(0);
        expect(results[0].entry.url).toBe("/api");
    });

    it("searches in keywords", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("beginner");

        expect(results.length).toBeGreaterThan(0);
        expect(results[0].entry.url).toBe("/getting-started");
    });

    it("searches in description", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("documentation");

        expect(results.length).toBeGreaterThan(0);
        const urls = results.map((r) => r.entry.url);
        expect(urls).toContain("/");
        expect(urls).toContain("/api");
    });

    it("respects maxResults option", () => {
        const search = new DocumentSearch(testIndex, { maxResults: 2 });
        const results = search.search("the");

        expect(results.length).toBeLessThanOrEqual(2);
    });

    it("returns results with scores", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("API");

        expect(results[0].score).toBeGreaterThan(0);
    });

    it("returns results sorted by score descending", () => {
        const search = new DocumentSearch(testIndex);
        const results = search.search("application");

        for (let i = 1; i < results.length; i++) {
            expect(results[i - 1].score).toBeGreaterThanOrEqual(results[i].score);
        }
    });
});

describe("DocumentSearch.suggest", () => {
    it("returns empty array for empty query", () => {
        const search = new DocumentSearch(testIndex);
        expect(search.suggest("")).toEqual([]);
        expect(search.suggest("   ")).toEqual([]);
    });

    it("returns suggestions for partial input", () => {
        const search = new DocumentSearch(testIndex);
        const suggestions = search.suggest("app");

        expect(suggestions.length).toBeGreaterThan(0);
    });

    it("respects maxSuggestions parameter", () => {
        const search = new DocumentSearch(testIndex);
        const suggestions = search.suggest("a", 2);

        expect(suggestions.length).toBeLessThanOrEqual(2);
    });
});

describe("createSearch", () => {
    it("creates search from object", () => {
        const search = createSearch(testIndex);
        expect(search.entryCount).toBe(4);
    });

    it("creates search from JSON string", () => {
        const search = createSearch(JSON.stringify(testIndex));
        expect(search.entryCount).toBe(4);
    });

    it("accepts custom options", () => {
        const search = createSearch(testIndex, { maxResults: 1 });
        const results = search.search("the");

        expect(results.length).toBeLessThanOrEqual(1);
    });
});
