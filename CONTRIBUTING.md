# Contributing

The following guidelines were chosen very deliberately to make sure the project benefits from contributions.
This is true for such diverse areas as a firm legal foundation or a sensible and helpful commit history.

* [Code of Conduct](#code-of-conduct)
* [Contributor License Agreement](#junit-pioneer-contributor-license-agreement)
* [If you're new...](#if-youre-new)
	* [...to Open Source](#to-open-source)
	* [...to JUnit Jupiter Extensions](#to-junit-jupiter-extensions)
	* [...to JUnit Pioneer](#to-junit-pioneer)
* [Writing Code](#writing-code)
	* [Code Organization](#code-organization)
	* [Code Style](#code-style)
	* [Tests](#tests)
	* [Documentation](#documentation)
	* [Git](#git)
* [Fixing Bugs, Developing Features](#fixing-bugs-developing-features)
	* [Branching Strategy](#branching-strategy)
	* [Commits](#commits)
	* [Pull Requests](#pull-requests)
	* [Merging](#merging)
	* [Commit Message](#commit-message)
* [Dependencies](#dependencies)
	* [JUnit 5](#junit-5)
	* [Others](#others)
* [Releases](#releases)
	* [Publishing](#publishing)
	* [Versioning](#versioning)
	* [Background](#background)
* [Pioneer Maintainers](#pioneer-maintainers)
	* [What We Do](#what-we-do)
	* [When We Do It](#when-we-do-it)
	* [Communication](#communication)
	* [Where The Buck Stops](#where-the-buck-stops)

The guidelines apply to maintainers as well as contributors!

## Code of Conduct

JUnit Pioneer uses a slightly adapted version of [the Contributor Covenant code of conduct](https://www.contributor-covenant.org/):

> We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, caste, color, religion, or sexual identity and orientation.
>
> We pledge to act and interact in ways that contribute to an open, welcoming, diverse, inclusive, and healthy community.

Please read on in [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for standards, scope, and enforcement.
The CoC binds maintainers, contributors, and other community members alike; in community spaces and in cases of stark violations also outside of them.

## JUnit Pioneer Contributor License Agreement

We don't have a dedicated contributor license agreement (CLA), but please consider [GitHub's terms of service](https://docs.github.com/en/site-policy/github-terms/github-terms-of-service#6-contributions-under-repository-license):

> Whenever you add Content to a repository containing notice of a license, you license that Content under the same terms, and you agree that you have the right to license that Content under those terms.

JUnit Pioneer uses the [Eclipse Public License v2.0](https:/eclipse.org/legal/epl-2.0/), which you can also find [here](https://github.com/junit-pioneer/junit-pioneer/blob/main/LICENSE.md) in this repository.

## If you're new...

First of all, welcome!
We really appreciate that you consider contributing to JUnit Pioneer.

### ...to Open Source

We know that this can be quite daunting at first:
Everybody uses a vocabulary and techniques that appear quite cryptic to those not steeped in them.
We can't fix that in a short file like this, but we want to provide some pointers to get you started.
If anything that follows in this document isn't clear, [open an issue](https://github.com/junit-pioneer/junit-pioneer/issues/new/choose) and ask us to explain it better.

To get you started, have a look at the [Open Source Guide](https://opensource.guide/) article [_How to Contribute to Open Source_](https://opensource.guide/how-to-contribute/).
We particularly recommend the following sections:

* [Orienting yourself to a new project](https://opensource.guide/how-to-contribute/#orienting-yourself-to-a-new-project)
* [How to submit a contribution](https://opensource.guide/how-to-contribute/#how-to-submit-a-contribution), especially
	* [Opening a pull request](https://opensource.guide/how-to-contribute/#opening-a-pull-request) (the links for [forking](https://guides.github.com/activities/forking/) and [branching](https://guides.github.com/introduction/flow/) are really helpful!)
* [What happens after you submit a contribution](https://opensource.guide/how-to-contribute/#what-happens-after-you-submit-a-contribution)

With (some of) the basics covered, let's turn to JUnit Pioneer:

* [`README.md`](README.md) and `CONTRIBUTING.md` are written in Markdown.
For information on how to use it, see [GitHub's documentation](https://guides.github.com/features/mastering-markdown/).
* The [feature documentation](#documentation) is written in AsciiDoctor.
For information on how to use it, check its [user manual](https://asciidoctor.org/docs/user-manual/) and [writer's guide](https://asciidoctor.org/docs/asciidoc-writers-guide/).

### ...to JUnit Jupiter Extensions

There are a couple of good guides to get you started on this:

* first of all, the [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/#extensions)
* then there's [Nicolai's article on the topic](https://nipafx.dev/junit-5-extension-model/)

### ...to JUnit Pioneer

If you want to get to know the project, we invite you to watch [our joint presentation on JUnit Pioneer](https://www.youtube.com/watch?v=6OBWn3_a0JQ) (~1 hour).
It's good. 😃

To get started, check [these good first issues](https://github.com/junit-pioneer/junit-pioneer/contribute).


## Writing Code

We have a few guidelines on how to organize, style, and document extensions.
Everything related to branches, commits, and more is described [further below](#fixing-bugs-developing-features).

### Code Organization

Where to put types and how to name them.

#### Package Structure

Classes usually belong into one of these packages:

* `org.junitpioneer.internal` - code intended to be shared across various extensions
* `org.junitpioneer.jupiter` - extensions to JUnit Jupiter
	* `....issue` - implementation details of issue extension
	* `....params` - extensions for Jupiter's `@ParameterizedTest`
* `org.junitpioneer.vintage` - extensions to older JUnit versions

If none of them is a good fit, we'll find one together.

#### Annotations

Many extensions will come with their own annotations.
These have to be top-level types, i.e. they have to be in their own source file with the annotation's name.
If an annotation is repeatable (e.g. `@ReportEntry`), the containing annotation (`ReportEntries`) must be placed in the same file as the repeatable annotation itself (`ReportEntry.java`).

#### Extension Classes

Classes implementing an extension's functionality should reflect that in their name:

* if a class (indirectly) implements `Extension`, it should end with that word
* if a class (indirectly) implements `ArgumentsProvider`, `ParameterResolver` or `InvocationContext`, it should end with that word

Note _should_, not _must_ - there can be exceptions if well argued.

#### Extension Scopes

Consider the following:

```java
@YourExtension
class MyTests {

	@Test
	void testFoo() { /* ... */ }

	@Test
	void testBar() { /* ... */ }

}
```

You might ask yourself: should `@YourExtension` run

1. once before/after all tests (meaning it "brackets" the test class, typically via [`BeforeAllCallback`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/BeforeAllCallback.html) / [`AfterAllCallback`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/AfterAllCallback.html)) or
2. once before/after each test (meaning it "brackets" each test method, typically via [`BeforeEachCallback`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/BeforeEachCallback.html) / [`AfterEachCallback`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/AfterEachCallback.html))?

We decided to _default_ to option 2, particularly for extensions that set and reset state (often global state like `DefaultLocaleExtension` and `DefaultTimezoneExtension`), as we believe this is less error-prone and covers more common use cases.
Furthermore, we want to guarantee consistent behavior across different extensions.

This, however, is just a default.
`@YourExtension` is free to diverge if it makes sense.

#### Namespaces

Interacting with [Jupiter's extension `Store`](https://junit.org/junit5/docs/current/user-guide/#extensions-keeping-state) requires a `Namespace` instance.
These should always be created from a class as follows:

```java
private static final Namespace NAMESPACE = Namespace.create(YourExtension.class);
```

It usually makes sense to store them in a static final field.

### Code Style

How to write the code itself.

#### `Optional`

[There shall be no `null` - use `Optional` instead](https://nipafx.dev/intention-revealing-code-java-8-optional/):

* design code to avoid optionality wherever feasibly possible
* in all remaining cases, prefer `Optional` over `null`

#### Reusability

We strive to make our extensions reusable and extensible.

A key ingredient in that is making sure that annotations work as meta-annotations (i.e. users can apply _our_ annotations to _their_ annotations and our extensions still work).
To achieve this, apply `@Target({ ElementType.ANNOTATION_TYPE })` to annotations and prefer `org.junitpioneer.internal.PioneerAnnotationUtils` and `org.junit.platform.commons.support.AnnotationSupport` when searching for annotations.

Another aspect is that annotations that apply to classes (i.e. those marked with `@Target({ ElementType.TYPE })`) should be inherited by subclasses.
For that, also add the annotation `@Inherited`.

**NOTE**:
`ElementType.TYPE` includes annotations, so there's no need to apply it _and_ `ElementType.ANNOTATION_TYPE`.

#### Thread-safety

It must be safe to use Pioneer's extensions in a test suite that is executed in parallel.
To that end it is necessary to understand [JUnit Jupiter's parallel execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution), particularly [the synchronization mechanisms it offers](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization): `@Execution` and `@ResourceLock`.

For extensions touching global state (like default locales or environment variables), we've chosen the following approach:

* the extension acquires a read/write lock to the global resource (this prevents extended tests from running in parallel)
* we offer a `@Writes...` annotation that does the same thing, so users can annotate their tests that write to the same resource and prevent them from running in parallel with each other and with extended tests
* we offer a `@Reads...` annotation that acquires read access to the same lock, so users can make sure such tests do not run in parallel with tests that write to the same resource (they can run in parallel with one another, though)

To have a better chance to discover threading-related problems in our extensions, we parallelize our own tests (configured in [`junit-platform.properties`](src/test/resources/junit-platform.properties)) .
Ideally, we'd like to run them in parallel _across_ and _within_ top-level classes, but unfortunately, [this leads to problems](https://github.com/junit-pioneer/junit-pioneer/pull/253#issuecomment-665235062) when some test setups change global state (like the security manager) that other tests rely on.
As we see it, the solution would be to force such tests onto a single thread, but [Jupiter has no such feature, yet](https://github.com/junit-team/junit5/issues/2142).
While a homegrown solution [is possible](https://github.com/junit-team/junit5/issues/2142#issuecomment-668409251), we wait for the discussion to resolve.
We hence do not parallelize across top-level classes - just within.

Most extensions verify their configuration at some point.
It helps with writing parallel tests for them if they do not change global state until the configuration is verified.
That particularly applies to "store in beforeEach - restore in afterEach"-extensions!
If they fail after "store", they will still "restore" and thus potentially create a race condition with other tests.

#### Compiler Warnings

The build is configured to treat almost all compiler warnings as errors (see below for exceptions).
If code that triggers a warning can't be refactored to avoid that, `@SuppressWarning` may be added, but we don't want to do that liberally.
Developers and reviewers should minimize its use.

Exceptions:
* `exports` - Pioneer's public API mentions a lot of Jupiter classes (e.g. all custom annotations use Jupiter's annotations), which leads to warnings that recommend to transitively require the corresponding Jupiter modules.
  Doing that would mean that Pioneer users wouldn't have to require Jupiter's modules, which is backwards - we're the appendix, here.
  Since we don't want to pepper `@SuppressWarning("exports")` everywhere, the warning is disabled.

### Tests

The name of test classes _must_ end with `Tests`, otherwise Gradle will ignore them.
The name of nested classes which are used as test fixture for executing Jupiter should end with `TestCases`, even when they only contain a single test method.

#### Assertions

All tests shall use [AssertJ](https://assertj.github.io/doc/)'s assertions and not the ones built into Jupiter:

* more easily discoverable API
* more detailed assertion failures

Yes, use it even if Jupiter's assertions are as good or better (c.f. `assertTrue(bool)` vs `assertThat(bool).isTrue()`) - that will spare us the discussion which assertion to use in a specific case.

Pioneer now has its own assertions for asserting not directly executed tests.
This means asserting `ExecutionResults`.
We can divide those kinds of assertions into two categories: test case assertions and test suite assertions.
 - Test case assertions are the ones where you assert a single test, e.g.: it failed with an exception or succeeded.
 For those, use the assertions that begin with `hasSingle...`, e.g.: `hasSingleSucceededTest()`.
 - Test suite assertions are the ones where you assert multiple tests and their outcomes, e.g.: three tests started, two failed, one succeeded.
 For those, use the assertions that begin with `hasNumberOf...`, e.g.: `hasNumberOfFailedTests(1)`.

Do not mix the two - while technically correct (meaning you _can_ write `hasNumberOfFailedTests(3).hasSingleSucceededTest()`) it is better to handle them separately.

### Documentation

There are several aspects of this project's documentation.
Some project-specific requirements apply to all non-`.java` files:

* one sentence per line, i.e. no matter how short or long a sentence is, it will occupy a single line, not shared with any other sentences
* to start a new paragraph, add a single blank line

#### Feature Documentation

Each feature is documented on [the project website](https://junit-pioneer.org/docs/), which is pulled from the files in the `docs/` folder, where each feature has:

* an entry in `docs-nav.yml` (lexicographically ordered)
* its own `.adoc` file

Add these entries when implementing a new feature and update them when changing an existing one.
The Javadoc on an extension's annotations should link back to the documentation on the website "for more information".

Code blocks in these files should not just be text.
Instead, in the `src/demo/java` source tree, create/update a `...Demo` class that is dedicated to a feature and place code snippets in `@Test`-annotated methods in `...Demo`.
Write each snippet as needed for the documentation and bracket it with tags:

```java
// tagging the entire test method:

// tag::$TAG_NAME[]
@Test
@SomePioneerExtension
void simple() {
	// demonstrate extension
}
// end::$TAG_NAME[]


// tagging a few lines from the test:

@Test
void simple() {
	// tag::$TAG_NAME[]
	SomePioneerExtension ex = // ...
	// demonstrate extension
	// end::$TAG_NAME[]
	assertThat(ex). // ...
}
```

Where feasible, include or follow up with assertions that ensure correct behavior.
Thus `...Demo` classes guarantee that snippets compile and (roughly) behave as explained.

In the documentation file, include these two attributes pointing at the demo source file:

```adoc
:xp-demo-dir: ../src/demo/java
:demo: {xp-demo-dir}/org/junitpioneer/jupiter/...Demo.java
```

It is **critically important** that the first attribute is called `xp-demo-dir` and that the second attribute references it.
Without this exact structure, the snippets will not show up on the website (even if they appear correctly in an IDE).

To include these snippets, use a block like the following:

```adoc
[source,java,indent=0]
----
include::{demo}[tag=$TAG_NAME]
----
```

#### README.md and CONTRIBUTING.md

Changes to project processes are usually reflected in these files (and are thus mostly done by maintainers).
One aspect that's relevant to contributors is the list of contributions at the end of [README.md](README.md) - if you fixed a bug or added a feature, please add yourself to that list in the following form:

```md
* [$NAME]($GITHUB-URL) $CONTRIBUTIONS
```

* `$NAME` can be your actual name or your GitHub account name
* `$GITHUB-URL` is the URL to your GitHub account
* `$CONTRIBUTIONS` is a list of one or two contributions (possibly with an appended "and more" if there are more)
	* for new features, link to the feature documentation on [junit-pioneer.org](https://junit-pioneer.org)
	* include issue and pull request IDs in the form `(#123 / #125)`

### Git

#### Line Endings

We [mind the end of our lines](https://adaptivepatchwork.com/2012/03/01/mind-the-end-of-your-line/) and have [instructed](.gitattributes) Git to replace all line endings with `LF` (the non-Windows variant) when writing files to the working directory.
If you're on Windows and prefer `CRLF` line endings, consider setting `core.autocrlf` to `true`:

```bash
git config --global core.autocrlf true
```


## Fixing Bugs, Developing Features

This section governs how features or bug fixes are developed.
See [the section _Updating Dependency on JUnit 5_](#junit-5) for how to adapt to upstream changes.

### Branching Strategy

By default, development happens in branches, which are merged via pull requests (PRs from `main` will be closed).
Special cases, like fixing problems with the CI pipeline, are of course exempt from this guideline.

Please make sure to give branches a meaningful name!
As an example, the one creating this documentation was called `branching-merging-documentation`.
For bonus points, adhere to the following rule.

For branches that are pushed directly to this repo (take note maintainers!), this naming strategy is obligatory:

* branches intended to fix an issue must be named `issue/$NUMBER-$TITLE` where `$NUMBER` is the issue number on GitHub and `$TITLE` a very short summary of what the issue is about (for those of us who don't know all issues by heart) in lower case letters and dash-separated, e.g. `issue/226-team-of-maintainers`
* branches intended to fix an issue that is already being worked on (hence the name is already blocked), copy its name but append an underscore and the maintainer's name, e.g. `issue/226-team-of-maintainers_bukama`
* branches intended to experiment with something, for which no issue exists, must be named `lab/$TITLE`, where `$TITLE` works like above

Issue branches should be deleted after they are merged.
Lab branches should be deleted once they become obsolete - when that is the case will be different for each branch.

### Commits

While it is nice to have each individual commit pass the build, this is not a requirement - it is the contributor's branch to play on.

See section [_Commit Message_](#commit-message) for how the commit message should look like.

### Pull Requests

Pull requests are used to discuss a concrete solution, not the motivation nor requirements for it.
As such there should be at least one issue a pull request relates to.
At the same time it should be focused so it should usually not relate to more than one issue (although that can occasionally happen).
Please mention all issues in the request's body, possibly using [closing keywords](https://help.github.com/articles/closing-issues-via-commit-messages/) like `closes`, `fixes` (for bugs only), or `resolves`.

The [pull requests template](.github/PULL_REQUEST_TEMPLATE.md) contains a footer that must not be edited or removed.

To enforce the [branching strategy](#branching-strategy) pull requests from `main` will be closed.

### Full Testing

In order to minimize the delay between a push and feedback, the default build is only run on a small subset of all possible builds (which include different operating system, Java versions and so on).
Once a pull request is ready to be merged, a maintainer needs to apply the _merge-ready_ label to trigger a full build.

### Merging

Only maintainers can merge pull requests, so PRs from contributors require that at least one maintainer agrees with the changes.
Ideally, two should give their thumbs up, though.
Likewise, PRs opened by a maintainer should be reviewed and agreed to by at least one other maintainer.
Going further, we should avoid merging PRs that one maintainer outright disagrees with and instead work towards a solution that is acceptable to everybody.
Note all the _should_-s - exceptions can be made if justifiable (e.g. maintainers don't react or there is reason to hurry).

A pull request is accepted by squashing the commits and fast-forwarding `main`, making each bug fix or feature appear atomically on `main`.
This can be achieved with GitHub's [_squash and merge_](https://help.github.com/articles/about-pull-request-merges/#squash-and-merge-your-pull-request-commits) feature.

### Commit Message

To make the single commit expressive, its message must be detailed and [good]((https://chris.beams.io/posts/git-commit/)) (really, read that post!).
Furthermore, it must follow this structure:

```
${action} (${issues} / ${pull-request}) [max 70 characters]

${body} [max 70 characters per line]

${references}: ${issues}
PR: ${pull-request}
```

`${action}` should succinctly describe what the PR does in good Git style.
Ideally, this title line (without issue and PR numbers) should not exceed 50 characters - 70 is the absolute maximum.
It is followed, in parentheses, by a comma-separated list of all related issues, a slash, and the pull request (to make all of them easy to find from a look at the log).

`${body}` should outline the problem the pull request was solving - it should focus on _why_ the code was written, not on _how_ it works.
This can usually be a summary of the issue description and discussion as well as commit messages.
Markdown syntax can be used and lines should usually not exceed 70 characters (exceptions are possible, e.g. to include stack traces).

Optionally, the message ends with a list of related issues:

* `${references}` is usually _Closes_, _Fixes_, or _Resolves_, but if none of that is the case, can also be _Issue(s)_
* `${issues}` is a comma-separated list of all related issues

This makes the related issues and pull request easy to find from a look at the log.

Once a pull request is ready to be merged, the contributor will be asked to propose an action and body for the squashed commit and the maintainer will refine them when merging.

As an example, the squashed commit 22996a2, which created this documentation, could have had the following message:

```
Document branching and merging (#30, #31 / #40)

To make sure the project has a sensible and helpful commit history and
interacts well with GitHub's features the strategy used for branching,
commit messages, and merging must be chosen carefully and deliberately.
The following aspects are particularly important:

 - a history that is accessible, detailed, and of high quality
 - backlinks from commits to isses and PRs without creating
   "notification noise" in the web interface
 - reduce necessity for maintainers policing contributors' commit
   messages

The chosen approach to squash and merge fulfills all of them except
the detailed history, which will be more coarse than with merge commits
or fast-forward merges. This was deemed acceptable in order to achieve
the other points, particularly the last one.

Closes: #30
Closes: #31
```

Finally, because of the noise it generates on the issue, please do _not_ mention the issue number in the message during development.

## Dependencies

### JUnit 5

JUnit Pioneer has an uncharacteristically strong relationship to the JUnit 5 project (often called _upstream_).
It not only depends on it, it also uses its internal APIs, copies source code that is not released in any artifact, mimics code style, unit testing, build and CI setup, and more.
As such it will frequently have to adapt to upstream changes, so it makes sense to provision for that in the development strategy.

#### Declaring Dependencies

JUnit Jupiter has few external dependencies, but occasionally uses them in its own API and thus has the `requires transitive` directive in [its module declaration](https://github.com/junit-team/junit5/blob/main/junit-jupiter-api/src/module/org.junit.jupiter.api/module-info.java) (for example, `requires transitive org.opentest4j_`).
That means, while JUnit Pioneer _could_ list these dependencies in its build configuration and require these modules in its module declaration, it doesn't _have to_.

It is generally recommended not to rely on transitive dependencies when they're used directly and instead manage them yourself, but this does not apply very well to Pioneer and Jupiter:

* If Jupiter stops using one of these dependencies, there is no point for us to keep using it as we only need them to integrate with Jupiter.
* If Jupiter refactors these module relationships (e.g. by removing the OpenTest4J module from its dependencies and pulling its code into a Jupiter module), we might not be compatible with that new version (e.g. because we still require the removed module, which now results in a split package)
* We can't choose a different dependency version than Jupiter

We hence only depend on "core Jupiter" explicitly.
That is:

* core API: _org.junit.jupiter.api_
* additional APIs as needed, e.g. _org.junit.jupiter.params_
* additional functionality as needed, e.g. _org.junit.platform.launcher_

#### Updating JUnit 5

As [documented](README.md#dependencies) Pioneer aims to use the lowest JUnit 5 version that supports Pioneer's feature set.
At the same time, there is no general reason to hesitate with updating the dependency if a new feature requires a newer version or the old version has a severe bug.
Follow these steps when updating JUnit 5:

* create a separate issue just for the update
	* explain which feature (i.e. other Pioneer issue) requires it
	* explain which changes in the Pioneer code base could result from that if you know about any; mention the upstream issue and PR that caused them
	* if changes are optional or not straightforward, allow for a discussion
* create a pull request for the update with just the changes caused by it
* the commit message...
	* ... should be structured and worded as defined above
	* ... should reference the upstream issue and pull request (if any)

### Others

JUnit Pioneer handles dependencies beyond JUnit 5 differently depending on how they impact its users.

#### For Execution

Pioneer avoids adding to users' dependency hell and hence doesn't take on dependencies beyond JUnit 5 that are _required_ at run time.
_Optional_ dependencies are acceptable if they are needed to provide specific features, particularly:

* to _integrate_ with other tools, frameworks, and libraries by offering features that directly interact with them (a hypothetical example is [Playwright](https://playwright.dev) for E2E testing)
* for _ease of use_ when recreating functionality would be too complex or otherwise out of scope for Pioneer (an example is [Jackson](https://github.com/FasterXML/jackson) for JSON parsing)

Unless we see reports of optional dependencies causing unexpected problems for users, there is no particularly high hurdle for taking them on, given each provides more than marginal value.
They should only be used by specifically chosen features that require them, though, and care needs to be taken to prevent them from creeping into the rest of the code base - CheckStyle rules need to be configured for each that fail the build on accidental use of these dependencies.

Optional dependencies are implemented with [Gradle's feature variants](https://docs.gradle.org/current/userguide/feature_variants.html).
Pioneer's module declaration must be extended with a matching `requires static` clause, which limits optional dependencies to those that have at least an explicit automatic module name.
Note that `requires static` does not suffice to pull in the optional dependency's module if no user code depends on it as well.

Each Pioneer feature that depends on them must profusely document that:

* in the feature documentation with configuration examples for Maven and Gradle (Kotlin suffices), including for the case where Pioneer is used on the module path and no other module depends on the optional dependency (i.e. explain how to configure `--add-modules`)
* in the Javadoc with a mention of the needed dependencies and the potential `--add-modules` directive (but no detailed guide how to accomplish either - link to website instead)
* in the case that the dependency is missing, with a clear error message that echoes the Javadoc

#### For Test and Build

Test dependencies like AssertJ and build dependencies on Gradle plugins do not impact users and are fair game.
Of course, we want to avoid our own dependency hell, so each dependency should still be carefully considered.

#### Updates

To keep dependencies up to date, run `gradle dependencyUpdates`, which lists all dependencies for which a newer version exists.
Updates then need to be done manually.
To keep the commit history clean, these should be done in bulk every few weeks.


## Releases

JUnit Pioneer uses [Shipkit](http://shipkit.org/) and [GitHub Actions](https://github.com/features/actions/) to automate the release process, but unlike Shipkit's default we don't release on every commit to `main`.
Instead, we take into account...

* whether a change demands a release (which is a low bar; basically anything that changes behavior does)
* whether more changes are going to arrive soon (often the case when we work on stream and merge a few PRs within a couple of hours)

The decision to publish a release and which version to pick can be made by any two maintainers.
Before publishing, they must check whether any `@since` tags were added since the last release and whether they reference the correct (i.e. upcoming) version.
(Ideally this happened when the PRs were merged, but this can be easily overlooked.)

### Publishing

Releases must be triggered manually with the [_Release build_ GitHub Action](https://github.com/junit-pioneer/junit-pioneer/actions/workflows/release-build.yml):

* select `main` branch
* specify the version (see next section)

GitHub Actions will then tell Gradle/Shipkit to do their thing.

Every new version is published to Maven Central and a release is created on GitHub.
This also triggers a website build - [see its `README.md`](https://github.com/junit-pioneer/junit-pioneer.github.io) for more information.

### Versioning

JUnit Pioneer uses semantic versioning, i.e. _major.minor.patch_ as follows:

* _major_: increases after team decision
* _minor_: resets to 0 when _major_ changes and increases for each substantial change or non-trivial feature
* _patch_: resets to 0 when _minor_ changes and increases otherwise

The Javadoc `@since` tag can guide whether a change is non-trivial.
If such a tag was added, _minor_ must increased - if not, it's up for debate (which is best held in a high-fidelity tool like Discord or Twitch chat).

For contributors that means that when they add members that require such a tag, they should generally put the next _minor_ version next to it.

**A note on Shipkit**:
[Shipkit's _auto-version_ plugin](https://github.com/shipkit/shipkit-auto-version) _can_ detect the version to be released on its own, but it increases the patch versions by number of commits since recent release (hence 1.3.0 ~> 1.3.8), which is not what we want.
We hence don't use it.
The other feature it provides is detecting the recent version (needed by [the _changelog_ plugin](https://github.com/shipkit/shipkit-changelog)), which we do by running `git describe --tags --abbrev=0`.

### Background

Like [Mockito](http://mockito.org/), JUnit Pioneer used Shipkit for a continuous delivery model, where every change on the `main` branch (for example when merging a pull request) triggered a release build that published a new version if the following criteria were met:

- the commit message doesn't contain `[ci skip-release]`
- all checks (e.g. tests) are successful
- at least one main artifact (that includes `...-source.jar` and `...-javadoc.jar`) has changed

Because this project's development often happens in sporadic bursts, where a lot of PRs are merged within a few hours, this approach lead to some superfluous releases.
We also weren't 100% successful in predicting whether Shipkit would make a release and so we started cluttering our commit messages with `[ci skip-release]`, which was a bit annoying.
Hence the change to the model described above.


## Pioneer Maintainers

JUnit Pioneer is maintained by a small team of people who work on it in their free time - see [`README.md`](README.md) for a list.

### What We Do

As maintainers, we may work on features, but it is perfectly ok to leave that to contributors.
Our main focus should be to keep the project moving forward:

* vet, label, relate, and reply to issues
* provide technical guidance for contributors in issues and PRs
* work on behind-the-scenes tasks like CI, documentation, etc.
* release new versions

While maintainers will naturally gravitate towards tasks they prefer working on, there is no formal separation of duties and everybody's opinion on every topic is valued.

### When We Do It

We all have a soft spot for the project, but we also have jobs, families, hobbies, and other human afflictions.
There's no expectation of availability!
This applies to users opening issues, contributors providing PRs, and other maintainers - none of them can _expect_ a maintainer to have time to reply to their request.

### Communication

These are the channels we use to communicate with one another, our contributors, and users - in decreasing order of importance:

1. [project website](https://junit-pioneer.org)
2. files in the repository (like [`README.md`](README.md) and this `CONTRIBUTING.md`)
3. Git commit messages
4. issues/PRs [on GitHub](https://github.com/junit-pioneer/junit-pioneer)
5. _#junit-pioneer_ channel [in Discord](https://discord.gg/rHfJeCF)
6. team calls (organized in said Discord)
7. occasional [Twitch streams](https://twitch.tv/nipafx)

Whatever channel is being used to discuss a topic, the goal is always to push a summary and the conclusion of that conversation up the list as far as possible.
This is particularly important for the last three channels - "document or didn't happen".
A few examples:

* when we discover a problem or possible feature on stream, a new GitHub issue will be created
* when a team call or Discord discussion shapes our opinion on an issue or PR, the discussion (not just the conclusion!) is summarized in the issue or PR (see [the comments on the ShipKit evaluation](https://github.com/junit-pioneer/junit-pioneer/issues/193#issuecomment-611620554) for an example)
* when a PR is merged, the commit message summarizes what it is about (see [_Commit Message_](#commit-message) above)
* when a decision regarding the project structure or the development processes is made, it is reflected in `README.md`, `CONTRIBUTING.md`, or another suitable file or even the website
* when a new feature is merged, documentation is added to the website

### Where The Buck Stops

This project was founded by [Nicolai Parlog](https://github.com/nicolaiparlog) (together with [Steve Moyer](https://github.com/smoyer64)) and I (surprise, it's me writing this section) maintained it almost exclusively for the first two years.
As such, I have the right to overrule any decision that was made by other maintainers.
This also translates to a duty to do that with any decision that could end up harming the project in any form.
On other words, the buck stops with me and, ultimately, I bare responsibility for all mistakes.
(Moral responsibility, that is - legally, nobody has any responsibility. 😉)
