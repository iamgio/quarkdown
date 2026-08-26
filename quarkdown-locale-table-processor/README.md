# locale-table-processor

This module contains the KSP processor that generates `Locale` tables at build time, based on `java.util.Locale`.

- `Languages`: English language names by ISO 639 code (e.g. `it` to `Italian`)
- `Territories`: English territory names by ISO 3166 country code (e.g. `IT` to `Italy`)

Bundling this data makes locale resolution, used by the `.doclang` function,
platform-independent and deterministic at runtime, with no dependency on the JDK.