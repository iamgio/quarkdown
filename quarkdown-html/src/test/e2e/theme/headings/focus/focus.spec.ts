import {checkHeadings} from "../index";
import {suite} from "../../../quarkdown";

const {test} = suite(__dirname);

test("h1 and h2 banners bleed past both slide edges with no top margin", (page) =>
    checkHeadings(
        page,
        {bleeds: true, hasTopMargin: false},
        {bleeds: true, hasTopMargin: false},
    ));
