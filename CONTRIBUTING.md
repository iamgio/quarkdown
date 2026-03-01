[Issues]: https://github.com/iamgio/quarkdown/issues
[Issue]: https://github.com/iamgio/quarkdown/issues
[Discussions]: https://github.com/iamgio/quarkdown/discussions
[Discussion]: https://github.com/iamgio/quarkdown/discussions
[wiki]: https://quarkdown.com/wiki
[documentation]: https://quarkdown.com/docs
[standard library]: https://github.com/iamgio/quarkdown/tree/main/quarkdown-stdlib/src/main/kotlin/com/quarkdown/stdlib


# Contributing to Quarkdown

Thanks for interest in contributing to Quarkdown, the Markdown-based typesetting system, and its ecosystem!

All types of contributions are encouraged and valued.
Please make sure to read the relevant section before making your contribution, as it will make it easier for maintainers to handle it.

> If you like the project, but don't have time to contribute, that's totally fine!
> You can still support us and show your appreciation by doing any of the following:
> - Star :star2: the project.
> - Post the project on social media.
> - Mention the project to others.


## Table of Contents

- [Questions](#questions)
- [Contributing via issues](#contributing-via-issues)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
- [Contributing via PR](#contributing-via-pr)
  - [Your first contribution](#your-first-contribution)
  - [Understanding the architecture](#understanding-the-architecture)
- [Styleguides](#styleguides)



## Questions

Before you ask a question, it is best to search for existing [Issues] or [Discussions] that might help you.

If you then still feel the need to ask a question and need clarification, we recommend the following:

- Open a [Discussion](https://github.com/iamgio/quarkdown/discussions/new/choose) or [Issue](https://github.com/iamgio/quarkdown/issues/new), depending on what you feel is more appropriate for your question.
- Provide as much context as you can about what you're running into.
- Provide project version, along with JVM version and OS if relevant.

We will then take care of the issue as soon as possible.



## Contributing via issues

> ### Legal Notice
> When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content and that the content you contribute may be provided under the project license.


### Reporting Bugs


#### Before submitting a bug report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we ask you to investigate carefully, collect information and describe the issue in detail in your report. Please complete the following steps in advance to help us fix any potential bug as fast as possible.

- Make sure that you are using the latest version.
- Determine if your bug is really a bug and not an error on your side.
- Check if there is not already an issue for your bug in [Issues].

#### Submitting

Open an [Issue] with a clear and descriptive title. The body should contain the following information:
- Your input
- The output or stack trace
- JVM version
- Operating system
- Can you reliably reproduce the issue? And can you also reproduce it with older versions?



### Suggesting Enhancements


#### Before submitting an enhancement

- Make sure that you are using the latest version.
- Check the [wiki] and [documentation] carefully to check if the functionality is already present.
- Check [Issues] to see if the enhancement has already been suggested. If it has, add a comment to the existing issue instead of opening a new one.
- Find out whether your idea fits with the scope and aims of the project.

#### Submitting


Open an [Issue] with a clear and descriptive title. The body should contain the following information:

- Provide a step-by-step description of the suggested enhancement in as many details as possible.
- Describe the current behavior and explain which behavior you expected to see instead. At this point you can also tell which alternatives do not work for you.
- Explain why this enhancement would be useful.



## Contributing via PR

### Your first contribution

> [!IMPORTANT]
> Please **open a PR only after opening an [Issue]** for the change you want to make, so that maintainers can give you feedback on whether your contribution is likely to be accepted and how it should be implemented.

The following list shows contributions that are highly welcome, in order of importance:

1. [Issues] labeled with `good first issue` or `help wanted`. These issues are usually easier to solve and are a good starting point for new contributors.

2. Improve the **documentation** of the [standard library], which will be shown in the auto-generated [documentation].  
   To have a preview of the generated documentation, you can run `gradlew quarkdocGenerate`

3. Improve performance of the pipeline.

4. Add new functions to the [standard library]. It's suggested to open an [enhancement suggestion](#suggesting-enhancements) first.

5. Add new [themes](https://github.com/iamgio/quarkdown/tree/main/quarkdown-html/src/main/scss).
   Please ensure your theme looks correctly on all document types (`plain`, `paged`, `slides`, `docs`)
   on the [Mock document](https://github.com/iamgio/quarkdown/tree/main/mock) and [Quarkdown's wiki](https://github.com/iamgio/quarkdown/tree/main/docs).

### Understanding the architecture

The architecture behind Quarkdown's core is explained in the wiki's [*Pipeline*](https://quarkdown.com/wiki/pipeline).


## Tooling

### Building

The project uses Gradle as its build system.
To build the project, always run:

```bash
./gradlew installDist
```

> [!WARNING]
> Avoid `./gradlew build`, always use `installDist` or `distZip` instead.

### Testing

To run the full test suite:

```bash
./gradlew test
```

You can also run tests for a specific module:

```bash
./gradlew :quarkdown-core:test
./gradlew :quarkdown-html:test
```

End-to-end tests are heavy and aren't included in `:test`. They can be run with:

```bash
./gradlew :quarkdown-html:e2eTest
```

Note that all tests are automatically run on every PR.

### Running the CLI

You can run the Quarkdown CLI directly via Gradle, without needing to build the project first:

```bash
./gradlew run --args="c <file.qd> [options] --libs quarkdown-libs/src/main/resources"
```

### Documentation

- To compile the [wiki](docs), run either of the following commands from the `docs` directory:

  - ```bash
    quarkdown c main.qd --clean
    ```

  - ```bash
    ./gradlew run --args="c main.qd --clean --libs ../quarkdown-libs/src/main/resources"
    ```

- To generate Quarkdoc documentation only for the standard library:

  ```bash
  ./gradlew quarkdocGenerate
  ```

- To generate Quarkdoc documentation for the whole project:

  ```bash
  ./gradlew quarkdocGenerateAll
  ```

## Styleguides

#### Kotlin code style

Quarkdown uses [ktlint](https://github.com/pinterest/ktlint) to ensure a consistent codestyle is kept across the whole project.

Upon opening a PR, `./gradlew ktlintCheck` is automatically run, and the checks must pass before the PR can be merged. You can also run `./gradlew ktlintFormat` to automatically fix any formatting issues in your code.

#### Commit messages

Please ensure your commit messages use the [imperative tense](https://cbea.ms/git-commit/#imperative)
and following the [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/) specification, so that they are clear and consistent across the project.


## Attribution
This file was inspired by [contributing.md](https://contributing.md/).
