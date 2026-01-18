import {testCustomFontApplication} from "../index";
import {suite} from "../../quarkdown";

const {test} = suite(__dirname);

test("applies custom font to main, preserves headings and code font", async (page) => {
    await testCustomFontApplication(page, false);
});
