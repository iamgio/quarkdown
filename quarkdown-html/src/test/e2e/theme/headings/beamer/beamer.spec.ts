import {checkHeadings} from "../index";
import {suite} from "../../../quarkdown";

const {test} = suite(__dirname);

test("h1 keeps its top margin within the slide, h2 banner bleeds with no top margin", (page) =>
    checkHeadings(
        page,
        {bleeds: false, hasTopMargin: true},
        {bleeds: true, hasTopMargin: false},
    ));
