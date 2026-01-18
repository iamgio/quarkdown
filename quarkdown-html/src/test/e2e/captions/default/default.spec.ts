import {suite} from "../../quarkdown";
import {assertFigureCaption, assertTableCaption} from "../index";

const {test} = suite(__dirname);

test("renders image figure caption on bottom", async (page) => {
    await assertFigureCaption(page, "img", "bottom");
});

test("renders code figure caption on bottom", async (page) => {
    await assertFigureCaption(page, "pre", "bottom");
});

test("renders table caption on bottom", async (page) => {
    await assertTableCaption(page, "bottom");
});
