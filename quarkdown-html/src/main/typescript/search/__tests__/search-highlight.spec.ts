import {describe, expect, it} from "vitest";
import {extractPreviewAroundMatch, highlightTerms, trimTitleFromContent,} from "../search-highlight";

describe("trimTitleFromContent", () => {
    it("removes title from start of content", () => {
        expect(trimTitleFromContent("Hello World\nThis is content", "Hello World")).toBe(
            "This is content"
        );
    });

    it("is case insensitive", () => {
        expect(trimTitleFromContent("HELLO WORLD\nContent", "hello world")).toBe("Content");
    });

    it("handles leading whitespace", () => {
        expect(trimTitleFromContent("  Title\nContent", "Title")).toBe("Content");
    });

    it("returns content unchanged if title not at start", () => {
        expect(trimTitleFromContent("Some other content", "Title")).toBe("Some other content");
    });

    it("returns content if title is null", () => {
        expect(trimTitleFromContent("Content", null)).toBe("Content");
    });

    it("returns content if content is empty", () => {
        expect(trimTitleFromContent("", "Title")).toBe("");
    });
});

describe("extractPreviewAroundMatch", () => {
    it("returns full content if shorter than max length", () => {
        expect(extractPreviewAroundMatch("Short content", ["term"], 300)).toBe("Short content");
    });

    it("returns beginning with ellipsis if no match found", () => {
        const longContent = "A".repeat(400);
        const result = extractPreviewAroundMatch(longContent, ["xyz"], 100);
        expect(result).toBe("A".repeat(100) + "…");
    });

    it("centers preview around first match", () => {
        const content = "A".repeat(200) + "MATCH" + "B".repeat(200);
        const result = extractPreviewAroundMatch(content, ["match"], 50);
        expect(result).toContain("MATCH");
        expect(result.startsWith("…")).toBe(true);
        expect(result.endsWith("…")).toBe(true);
    });

    it("adds ellipsis at start when not at beginning", () => {
        const content = "A".repeat(100) + "MATCH" + "B".repeat(50);
        const result = extractPreviewAroundMatch(content, ["match"], 100);
        expect(result.startsWith("…")).toBe(true);
    });

    it("adds ellipsis at end when not at end", () => {
        const content = "MATCH" + "A".repeat(200);
        const result = extractPreviewAroundMatch(content, ["match"], 100);
        expect(result.endsWith("…")).toBe(true);
    });

    it("finds earliest match when multiple terms", () => {
        const content = "First APPLE then BANANA";
        const result = extractPreviewAroundMatch(content, ["banana", "apple"], 300);
        expect(result).toBe("First APPLE then BANANA");
    });
});

describe("highlightTerms", () => {
    it("wraps matched terms in strong tags", () => {
        expect(highlightTerms("Hello world", ["world"])).toBe("Hello <strong>world</strong>");
    });

    it("is case insensitive", () => {
        expect(highlightTerms("Hello WORLD", ["world"])).toBe("Hello <strong>WORLD</strong>");
    });

    it("highlights multiple occurrences", () => {
        expect(highlightTerms("foo bar foo", ["foo"])).toBe(
            "<strong>foo</strong> bar <strong>foo</strong>"
        );
    });

    it("highlights multiple different terms", () => {
        expect(highlightTerms("foo bar baz", ["foo", "baz"])).toBe(
            "<strong>foo</strong> bar <strong>baz</strong>"
        );
    });

    it("escapes HTML in non-matched parts", () => {
        expect(highlightTerms("<b>foo</b> bar", ["bar"])).toBe(
            "&lt;b&gt;foo&lt;/b&gt; <strong>bar</strong>"
        );
    });

    it("escapes HTML in matched parts", () => {
        expect(highlightTerms("<script>", ["<script>"])).toBe(
            "<strong>&lt;script&gt;</strong>"
        );
    });

    it("returns escaped text if no terms", () => {
        expect(highlightTerms("<div>text</div>", [])).toBe("&lt;div&gt;text&lt;/div&gt;");
    });

    it("handles longer terms first to avoid partial matches", () => {
        expect(highlightTerms("javascript is great", ["java", "javascript"])).toBe(
            "<strong>javascript</strong> is great"
        );
    });
});
