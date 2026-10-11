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
