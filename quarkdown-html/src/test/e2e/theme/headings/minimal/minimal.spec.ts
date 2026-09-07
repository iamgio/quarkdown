import {checkHeadings} from "../index";
import {suite} from "../../../quarkdown";

const {test} = suite(__dirname);

test("h1 and h2 keep their top margin", (page) =>
    checkHeadings(page, {hasTopMargin: true}, {hasTopMargin: true}));
