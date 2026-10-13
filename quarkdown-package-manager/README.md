# package-manager

This module resolves and downloads Quarkdown packages, called **atoms**, from a package repository.

## Atoms

An atom is a directory of Quarkdown sources and assets, published under a name and a version.

The official repository is [`quarkdown-labs/atoms`](https://github.com/quarkdown-labs/atoms), laid out as:

```
<name>/<version>/
    atom.toml      manifest
    main.qd        entry point
    ...            any other text or binary file
```

Files and directories whose name starts with `__` (such as `__tests`) are internal to the repository and are not installed.

## Resolving from `.include`

`LocalPackageResolver` implements core's `PackageResolver`, which the compiler consults when a document includes a
`@scope/name:version` reference. `locate` checks `<project>/@<scope>/<name>/atom.toml` against the referenced version;
`install` fetches the package from the repository registered for its scope and hands it to a `PackageWriter`.

Writers own all file-system writes. `DiskPackageWriter` (JVM) stages files in a temporary sibling directory and swaps it
into place, so an interrupted download never leaves a half-written package. `VirtualFileSystemPackageWriter` targets an
in-memory `VirtualFileSystem`, for tests and for hosts that keep the project in memory.

The CLI wires `LocalPackageResolver.disk()` into `PipelineOptions.packageResolver`. Other hosts pass their own resolver.
