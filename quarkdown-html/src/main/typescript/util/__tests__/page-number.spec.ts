import { describe, it, expect } from "vitest";
import { formatPageNumber } from "../page-number";

describe("formatPageNumber", () => {
  it("formats arabic numbers", () => {
    expect(formatPageNumber(5, "1")).toBe("5");
  });

  it("formats lower-alpha", () => {
    expect(formatPageNumber(1, "a")).toBe("a");
    expect(formatPageNumber(26, "a")).toBe("z");
  });

  it("formats upper-alpha", () => {
    expect(formatPageNumber(1, "A")).toBe("A");
    expect(formatPageNumber(26, "A")).toBe("Z");
  });

  it("formats lower-roman", () => {
    expect(formatPageNumber(1, "i")).toBe("i");
    expect(formatPageNumber(4, "i")).toBe("iv");
    expect(formatPageNumber(1999, "i")).toBe("mcmxcix");
  });

  it("formats upper-roman", () => {
    expect(formatPageNumber(9, "I")).toBe("IX");
  });

  it("returns the format string for unknown formats", () => {
    expect(formatPageNumber(3, "x")).toBe("x");
  });
});
