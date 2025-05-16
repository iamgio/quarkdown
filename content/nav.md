---
title: "Quarkdown"
logo_link: "https://quarkdown.com/"
logo_image: "https://raw.githubusercontent.com/iamgio/quarkdown/refs/heads/project-files/images/tbanner-dark.svg"
left_links:
  - "Themes, /themes, false"
  - "Pricing, /pricing, false"
right_links:
  - "Wiki, https://github.com/iamgio/quarkdown/wiki"
  - "Docs, https://quarkdown.com/docs"
buttons:
  - "Get started, https://github.com/iamgio/quarkdown?tab=readme-ov-file#getting-started"
---

# Navigation Configuration Field Explanation

- **title**: The main title for the navigation section, typically describing the purpose of the navigation configuration, in this case, "Navigation Configuration."
- **logo_link**: The URL where the logo links to when clicked. This is typically the homepage or the main site URL, in this case, "https://mdfriday.com/"
- **logo_image**: The path to the logo image file used in the navigation bar. This is the image that will be displayed as the logo in the navigation, here it is "img/mdfriday.svg."
- **left_links**: A list of links that will appear on the left side of the navigation bar. Each link is described by a title, URL, and a flag indicating whether the link is ready to be used (`true` means the link is ready and functional, `false` means the link is not ready, and may trigger an action like `onclick` instead of a direct URL).
    - Example: `"Themes, /themes, false"` means there is a "Themes" link that leads to `/themes`, but it is not yet ready (potentially triggering an action like `onclick`).
- **right_links**: A list of links that will appear on the right side of the navigation bar. Similar to `left_links`, but typically used for links like "Contact Us," external URLs, etc.
    - Example: `"Contact Us, mailto:service@mdfriday.com"` means there is a "Contact Us" link that opens the user's email client with the provided email address.
- **buttons**: A list of buttons that can be displayed in the navigation bar. Each button is defined by a text label, URL or action link, and a flag indicating if it’s ready (`true` means the button’s action or link is ready, `false` means it is not ready and might trigger an action instead).
    - Example: `"Get started, #, false"` means there is a "Get started" button with a `#` link that is not yet ready (potentially triggering an action like `onclick`).
