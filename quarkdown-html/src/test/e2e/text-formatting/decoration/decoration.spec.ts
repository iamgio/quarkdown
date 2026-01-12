import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies text decoration styles", async (page) => {
    await expect(page.getByText("underline", {exact: true})).toHaveCSS("text-decoration-line", "underline");
    await expect(page.getByText("overline", {exact: true})).toHaveCSS("text-decoration-line", "overline");
    await expect(page.getByText("underoverline", {exact: true})).toHaveCSS("text-decoration-line", "underline overline");
    await expect(page.getByText("strikethrough", {exact: true})).toHaveCSS("text-decoration-line", "line-through");
    await expect(page.getByText("all", {exact: true})).toHaveCSS("text-decoration-line", "underline overline line-through");
});
