[Issues]: https://github.com/iamgio/quarkdown/issues
[Issue]: https://github.com/iamgio/quarkdown/issues
[Discussions]: https://github.com/iamgio/quarkdown/discussions
[Discussion]: https://github.com/iamgio/quarkdown/discussions
[wiki]: https://github.com/iamgio/quarkdown/wiki
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

The following list shows contributions that are highly welcome, in order of importance:

1. Improve the **documentation** of the [standard library], which will be shown in the auto-generated [documentation].  
   To have a preview of the generated documentation, you can run `gradlew quarkdocGenerate`

2. Add new functions to the [standard library]. It's suggested to open an [enhancement suggestion](#suggesting-enhancements) first.

3. Add new [themes](https://github.com/iamgio/quarkdown/tree/main/quarkdown-core/src/main/resources/render/theme).
   Please ensure your theme looks correctly on all document types (`plain`, `paged`, `slides`)
   on the [Mock document](https://github.com/iamgio/quarkdown/tree/main/mock) or another comprehensive document.

  > [!WARNING]
  > The theme system will undergo an internal refactor soon.
  > Please beware that having many themes will make the refactor process slower.

### Understanding the architecture

The architecture behind Quarkdown's core is explained in the wiki's [*Pipeline*](https://github.com/iamgio/quarkdown/wiki/pipeline).
However, contributions to Quarkdown's core are not the priority at the moment.


## Styleguides

#### Kotlin code style

Quarkdown uses [ktlint](https://github.com/pinterest/ktlint) to ensure a consistent codestyle is kept across the whole project.

Upon opening a PR, `gradlew ktlintCheck` is automatically run.

#### Commit messages

Please ensure your commit messages use the [imperative tense](https://cbea.ms/git-commit/#imperative).



## Attribution
This file was inspired by [contributing.md](https://contributing.md/).
