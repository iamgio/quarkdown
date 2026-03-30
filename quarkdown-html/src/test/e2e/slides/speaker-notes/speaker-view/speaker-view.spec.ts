import {suite} from "../../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix(
    "speaker notes are accessible in the speaker view",
    ["slides"],
    async (page) => {
        // Inline .speaker-notes should be empty when showNotes is not enabled
        const speakerNotes = page.locator(".speaker-notes");
        await expect(speakerNotes).toBeEmpty();

        // Open speaker view by pressing S
        const popupPromise = page.context().waitForEvent("page");
        await page.keyboard.press("s");
        const speakerPage = await popupPromise;
        await speakerPage.waitForLoadState("domcontentloaded");

        const notesContainer = speakerPage.locator(".speaker-controls-notes");
        await expect(notesContainer).toBeVisible({timeout: 10000});

        // The speaker view shows the current slide's notes
        await expect(notesContainer).toContainText("hello");
    }
);
