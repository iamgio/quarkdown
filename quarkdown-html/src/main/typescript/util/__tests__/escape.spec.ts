import {describe, expect, it} from "vitest";
import {escapeHtml, escapeRegExp} from "../escape";

describe("escapeHtml", () => {
    it("escapes HTML special characters", () => {
        expect(escapeHtml("<script>alert('xss')</script>")).toBe(
            "&lt;script&gt;alert('xss')&lt;/script&gt;"
        );
    });

    it("escapes ampersands", () => {
        expect(escapeHtml("foo & bar")).toBe("foo &amp; bar");
    });

    it("preserves quotes (DOM-based escaping)", () => {
        // DOM-based escaping via textContent/innerHTML doesn't escape quotes
        expect(escapeHtml('"quoted"')).toBe('"quoted"');
    });

    it("returns empty string for empty input", () => {
        expect(escapeHtml("")).toBe("");
    });

    it("leaves plain text unchanged", () => {
        expect(escapeHtml("Hello World")).toBe("Hello World");
    });
});

describe("escapeRegExp", () => {
    it("escapes special regex characters", () => {
        expect(escapeRegExp("foo.*bar")).toBe("foo\\.\\*bar");
    });

    it("escapes parentheses and brackets", () => {
        expect(escapeRegExp("(test)[0]")).toBe("\\(test\\)\\[0\\]");
    });

    it("escapes question marks and plus signs", () => {
        expect(escapeRegExp("a+b?c")).toBe("a\\+b\\?c");
    });

    it("escapes dollar and caret", () => {
        expect(escapeRegExp("^start$end")).toBe("\\^start\\$end");
    });

    it("escapes curly braces and pipe", () => {
        expect(escapeRegExp("{a|b}")).toBe("\\{a\\|b\\}");
    });

    it("escapes backslashes", () => {
        expect(escapeRegExp("path\\to\\file")).toBe("path\\\\to\\\\file");
    });

    it("leaves plain text unchanged", () => {
        expect(escapeRegExp("hello world")).toBe("hello world");
    });
});
