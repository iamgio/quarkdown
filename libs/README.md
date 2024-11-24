# libs

This module contains libraries written in Quarkdown ([main/resources](main/resources)).

The Gradle build system will automatically copy these `.qmd` files into the `lib/qmd` directory of the distribution zip.  
At runtime, the CLI option `-l` or `--libs` sets the path to the library directory to use, which clearly defaults to `lib/qmd`.

In order to load a library from the library directory, the `.include {name}` function can be used.
After the library is loaded, the functions defined in the library can be used in the main document.

See [wiki: *Importing external libraries*](https://github.com/iamgio/quarkdown/wiki/importing-external-libraries) for further information.