import {checkHeadingExtension} from "../index";
import {suite} from "../../../quarkdown";

const {test} = suite(__dirname);

test("h1 banner stays within the slide, h2 background bleeds past both edges", (page) =>
    checkHeadingExtension(page, false));
