---
name: copy-changelog-sponsors
description: Copy the Sponsors block from the most recent released version in CHANGELOG.md into the Unreleased section. Use when the user asks to add, copy, or carry over sponsors to the Unreleased section of the changelog.
---

# Copy changelog sponsors

Appends the Sponsors block from the latest released version into the `## [Unreleased]` section of `CHANGELOG.md`, matching the format used by prior releases.

## Steps

1. Read `CHANGELOG.md` at the repo root.
2. Locate the `## [Unreleased]` section and the first released section below it (e.g. `## [2.0.1] - YYYY-MM-DD`).
3. Copy the Sponsors block from that released section verbatim. The block has this shape:

   ```markdown
   * * *

   ### Sponsors

   Thanks to our sponsors! 🎉

   @handle1

   @handle2

   <a href="https://falconer.com"><img src="https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/sponsors/falconer.jpeg" alt="Falconer" width="350"></a>
   ```

4. Insert the block at the end of the Unreleased section, immediately before the next `## [version]` heading. Keep a blank line above and below the inserted block.
5. If the Unreleased section already contains a Sponsors block, do nothing and report that to the user.

## Notes

- Copy verbatim, including handles, image URL, width, and the `🎉` emoji. Do not "improve" the wording.
- The `* * *` horizontal rule is part of the block and must be included.
- Do not edit any released section.
