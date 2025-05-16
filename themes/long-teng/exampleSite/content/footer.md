---
description: |
  MDFriday allows everyone to build professional, secure, multilingual sites—no need to worry about SSL certificates, servers, SEO, or security.
  Just a few clicks, and your website is ready.
address: "Wuhan, China"
links:
  - "Resources, Pricing, /pricing, false"   
  - "Social Media, Bilibili, https://space.bilibili.com/491786455, true"
---

# Footer Configuration Field Explanation

- **description**: A detailed description of the product or service that appears in the footer section. It explains the core features or benefits of the product, helping users understand what the service offers. In this case, it emphasizes the ease of building professional, secure websites with MDFriday.
- **address**: The physical location of the company or service, displayed in the footer. Here, it shows "Wuhan, China."
- **links**: A list of links to be displayed in the footer section. Each entry consists of a category, a link's title, URL, and a flag indicating whether the link is ready to be used (`true` means the link is ready, `false` means the link is not yet ready and might trigger an action like `onclick`). Links under the same category share the same category name but can have different URLs.
    - Example: `"Resources, Pricing, /pricing, false"` means there is a "Resources" category with a link titled "Pricing" that leads to `/pricing`, but it is not ready (likely will trigger an action instead of navigating).
    - Example: `"Resources, Documentation, /docs, true"` means there is a "Resources" category with a link titled "Documentation" that leads to `/docs`, and it is ready for use.
    - Example: `"Social Media, Bilibili, https://space.bilibili.com/491786455, true"` means there is a "Social Media" category with a link titled "Bilibili" that leads to the provided URL, and it is ready to be used.

#### Notes:
- Links within the same category (e.g., multiple "Resources" links) are listed consecutively under the same category name.
- The category name (e.g., "Resources") appears in the first position, followed by the link title, URL, and readiness flag.
