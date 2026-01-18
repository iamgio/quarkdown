import {suite} from "../../quarkdown";
import {assertFigureCaption, assertTableCaption} from "../index";

const {test} = suite(__dirname);

test("renders image figure caption on top", async (page) => {
    await assertFigureCaption(page, "img", "top");
});

test("renders code figure caption on top", async (page) => {
    await assertFigureCaption(page, "pre", "top");
});

test("renders table caption on top", async (page) => {
    await assertTableCaption(page, "top");
});
