# native-library-processor

This module contains the compile-time processor for native (Kotlin) Quarkdown libraries, such as the [stdlib](../quarkdown-stdlib),
with support for:

- `@QModule`/`@QFunction` registration
- `@Name` mapping
- Compile-time argument conversion via `ValueFactory`

See [ARCHITECTURE.md](ARCHITECTURE.md) for the pipeline and the data flow through a function call.
