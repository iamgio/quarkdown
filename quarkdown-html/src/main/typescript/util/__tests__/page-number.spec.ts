import {describe, expect, it} from "vitest";
import {formatNumber} from "../numbering";

describe("formatNumber", () => {
  it("formats arabic numbers", () => {
    expect(formatNumber(5, "1")).toBe("5");
  });

  it("formats lower-alpha", () => {
    expect(formatNumber(1, "a")).toBe("a");
    expect(formatNumber(26, "a")).toBe("z");
  });

  it("formats upper-alpha", () => {
    expect(formatNumber(1, "A")).toBe("A");
    expect(formatNumber(26, "A")).toBe("Z");
  });

  it("formats lower-roman", () => {
    expect(formatNumber(1, "i")).toBe("i");
    expect(formatNumber(4, "i")).toBe("iv");
    expect(formatNumber(1999, "i")).toBe("mcmxcix");
  });

  it("formats upper-roman", () => {
    expect(formatNumber(9, "I")).toBe("IX");
  });

  it("returns the format string for unknown formats", () => {
    expect(formatNumber(3, "x")).toBe("x");
  });
});
