import {checkHeadingExtension} from "../index";
import {suite} from "../../../quarkdown";

const {test} = suite(__dirname);

test("h1 and h2 backgrounds bleed past both slide edges", (page) => checkHeadingExtension(page, true));
