import {Locator} from "@playwright/test";
import {suite} from "../../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

async function assertNoteContent(note: Locator) {
    await expect(note).toContainText("Formatted");
    const bullets = note.locator("li");
    await expect(bullets).toHaveCount(3);
    await expect(bullets.nth(0)).toHaveText("Bullet 1");
    await expect(bullets.nth(1)).toHaveText("Bullet 2");
    await expect(bullets.nth(2)).toHaveText("Bullet 3");
}

testMatrix(
    "speaker notes are visible on each slide",
    ["slides", "slides-print"],
    async (page, docType) => {
        const notes = page.locator(".speaker-notes");

        if (docType === "slides-print") {
            // In print mode, each slide has its own .speaker-notes element
            await expect(notes).toHaveCount(3);

            await expect(notes.nth(0)).toContainText("hello");
            await assertNoteContent(notes.nth(1));

            // Slide 3 has no speaker note
            await expect(notes.nth(2)).toBeEmpty();
        } else {
            // In regular slides mode, .speaker-notes shows the current slide's note
            await expect(notes).toContainText("hello");

            await page.keyboard.press("ArrowRight");
            await assertNoteContent(notes);

            // Slide 3 has no speaker note
            await page.keyboard.press("ArrowRight");
            await expect(notes).toContainText("No notes on this slide.");
        }
    }
);
