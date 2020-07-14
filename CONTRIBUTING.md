# Contributing

The following guidelines were chosen very deliberately to make sure the project benefits from contributions.
This is true for such diverse areas as a firm legal foundation or a sensible and helpful commit history.

* [Contributor License Agreement](#junit-pioneer-contributor-license-agreement)
* [If you're new to Open Source](#if-youre-new-to-open-source)
* [Writing Code](#writing-code)
	* [Code Organization](#code-organization)
	* [Code Style](#code-style)
	* [Documentation](#documentation)
* [Fixing Bugs, Developing Features](#fixing-bugs-developing-features)
	* [Branching Strategy](#branching-strategy)
	* [Commits](#commits)
	* [Pull Requests](#pull-requests)
	* [Merging](#merging)
	* [Commit Message](#commit-message)
* [Updating Dependency on JUnit 5](#updating-dependency-on-junit-5)
* [Publishing](#publishing)
	* [Versioning](#versioning)
* [Pioneer Maintainers](#pioneer-maintainers)
	* [What We Do](#what-we-do)
	* [When We Do It](#when-we-do-it)
	* [Communication](#communication)
	* [Where The Buck Stops](#where-the-buck-stops)

The guidelines apply to maintainers as well as contributors!


## JUnit Pioneer Contributor License Agreement

**Project License:** [Eclipse Public License v2.0](LICENSE.md)

* You will only submit contributions where you have authored 100% of the content.
* You will only submit contributions to which you have the necessary rights.
  This means that if you are employed you have received the necessary permissions from your employer to make the contributions.
* Whatever content you contribute will be provided under the project license(s).

## If you're new to Open Source

First of all, welcome!
We really appreciate that you consider contributing to JUnit Pioneer.

We know that this can be quite daunting at first:
Everybody uses a vocabulary and techniques that appear quite cryptic to those not steeped in them.
We can't fix that in a short file like this, but we want to provide some pointers to get you started.
If anything that follows in this document isn't clear, [open an issue](https://github.com/junit-pioneer/junit-pioneer/issues/new) and ask us to explain it better.

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

## Writing Code

We have a few guidelines on how to organize, style, and document extensions. 
Everything related to branches, commits, and more is described [further below](#fixing-bugs-developing-features).

### Code Organization

Where to put types and how to name them.

#### Annotations

Many extensions will come with their own annotations.
These have to be top-level types, i.e. they have to be in their own source file with the annotation's name.
If an annotation is repeatable (e.g. `@ReportEntry`), the containing annotation (`ReportEntries`) must be placed in the same file as the repeatable annotation itself (`ReportEntry.java`).

#### Extension Classes

Classes implementing an extension's functionality should reflect that in their name:

* if a class (indirectly) implements `Extension`, it should end with that word
* if a class (indirectly) implements `ArgumentsProvider`, `ParameterResolver` or `InvocationContext`, it should end with that word

Note _should_, not _must_ - there can be exceptions if well argued.

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

[There shall be no `null` - use `Optional` instead](https://blog.codefx.org/techniques/intention-revealing-code-java-8-optional/):

* design code to avoid optionality wherever feasibly possible
* in all remaining cases, prefer `Optional` over `null`

#### Assertions

All tests shall use [AssertJ](https://joel-costigliola.github.io/assertj/)'s assertions and not the ones build into Jupiter:

* more easily discoverable API
* more detailed assertion failures

Yes, use it even if Jupiter's assertions are as good or better (c.f. `assertTrue(bool)` vs `assertThat(bool).isTrue()`) - that will spare us the discussion which assertion to use in a specific case.

Pioneer now has its own assertions for asserting not directly executed tests.
This means asserting `ExecutionResults`.
We can divide those kinds of assertions into two categories: test case assertions and test suite assertions.
 - Test case assertions are the ones where you assert a single test, e.g.: it failed with an exception or succeeded.
 For those, use the assertions that being with `hasSingle...`, e.g.: `hasSingleSucceededTest()`.
 - Test suite assertions are the ones where you assert multiple tests and their outcomes, e.g.: three tests started, two failed, one succeeded.
 For those, use the assertions that being with `hasNumberOf...`, e.g.: `hasNumberOfFailedTests(1)`.

Do not mix the two - while technically correct (meaning you _can_ write `hasNumberOfFailedTests(3).hasSingleSucceededTest()`) it is better to handle them separately.

### Documentation

There are several aspects of this project's documentation.
Some project-specific requirements apply to all non-`.java` files:

* one sentence per line, i.e. no matter how short or long a sentence is, it will occupy a single line, not shared with any other sentences
* to start a new paragraph, add a single blank line

#### Feature Documentation

Each feature is documented on [the project website](https://junit-pioneer.org/docs/), which is pulled from the files in the `docs/` folder, where each feature has:

* an entry in `docs-nav.yml` (lexicographically ordered)
* it's own `.adoc` file

Add these entries when implementing a new feature and update them when changing an existing one.
The Javadoc on an extension's annotations should link back to the documentation on the website "for more information".

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

#### Release Notes

Do **not** update the `release-notes.md` file!
This file is generated automatically.


## Fixing Bugs, Developing Features

This section governs how features or bug fixes are developed.
See [the section _Updating Dependency on JUnit 5_](#updating-dependency-on-junit-5) for how to adapt to upstream changes.

### Branching Strategy

By default, development happens in branches, which are merged via pull requests (PRs from `master` will be closed).
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

As a general rule, the style and formatting of commit messages should follow the [guidelines for good Git commit messages](http://chris.beams.io/posts/git-commit/).
Because of the noise it generates on the issue, please do _not_ mention the issue number in the message.

### Pull Requests

Pull requests are used to discuss a concrete solution, not the motivation nor requirements for it.
As such there should be at least one issue a pull request relates to.
At the same time it should be focused so it should usually not relate to more than one issue (although that can occasionally happen).
Please mention all issues in the request's body, possibly using [closing keywords](https://help.github.com/articles/closing-issues-via-commit-messages/) like `closes`, `fixes` (for bugs only), or `resolves`.

The [pull requests template](.github/PULL_REQUEST_TEMPLATE.md) contains a footer that must not be edited or removed.

To enforce the [branching strategy](#branching-strategy) pull requests from `master` will be closed.

### Merging

Only maintainers can merge pull requests, so PRs from contributors require that at least one maintainer agrees with the changes.
Ideally, two should give their thumbs up, though.
Likewise, PRs opened by a maintainer should be reviewed and agreed to by at least one other maintainer.
Going further, we should avoid merging PRs that one maintainer outright disagrees with and instead work towards a solution that is acceptable to everybody.
Note all the _should_-s - exceptions can be made if justifiable (e.g. maintainers don't react or there is reason to hurry).

A pull request is accepted by squashing the commits and fast-forwarding master, making each bug fix or feature appear atomically on master.
This can be achieved with GitHub's [_squash and merge_](https://help.github.com/articles/about-pull-request-merges/#squash-and-merge-your-pull-request-commits) feature.

### Commit Message

To make the single commit expressive, its message must be detailed and [good]((http://chris.beams.io/posts/git-commit/)) (really, read that post!).
Furthermore, it must follow this structure:

```
${action} (${issues} / ${pull-request})

${body}

${references}: ${issues}
PR: ${pull-request}
```

`${action}` should succinctly describe what the PR does in good Git style.
Ideally, this title line (without issue and PR numbers) should not exceed 50 characters - 70 is the absolute maximum.
It is followed, in parenthesis, by a comma-separated list of all related issues, a slash, and the pull request (to make all of them easy to find from a look at the log).

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

## Updating Dependency on JUnit 5

JUnit Pioneer has an uncharacteristically strong relationship to the JUnit 5 project (often called _upstream_).
It not only depends on it, it also uses its internal APIs, copies source code that is not released in any artifact, mimics code style, unit testing, build and CI setup, and more.
As such it will frequently have to adapt to upstream changes, so it makes sense to provision for that in the development strategy.

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


## Publishing

JUnit Pioneer uses [Shipkit](http://shipkit.org/) and [GitHub Actions](https://github.com/features/actions/) to automate the release process, but unlike Shipkit's default we don't release on every commit to `master`.
Instead, releases must be triggered manually:

1. make sure that the file [`version-properties`](version.properties) defines the correct version (see next section)
2. push a tag `releaseTrigger` to `master`

GitHub Actions will then tell Shipkit to do its thing and afterwards delete the tag.
The tag is always deleted, even if the release fails, so it is easy to trigger another one.

Every new version is published to the `junit-pioneer/maven` Bintray repository as well as to Maven Central and JCenter.
This also triggers a website build - [see its `README.md`](https://github.com/junit-pioneer/junit-pioneer.github.io) for more information.

### Versioning

Shipkit manages versions by reading from and writing to [`version-properties`](version.properties):
On each build, it releases the version specified by the `version` field and then increases its patch level for the next release.

This is how JUnit Pioneer handles versioning:

* _patch_: automatically increased by Shipkit on each release
* _minor_: manually increased for each substantial change/feature
* _major_: stays at 0 for now

That means, for now, contributors only have to care about _minor_.
Since each non-trivial change is developed in a PR, this is the place to discuss whether the minor version should be increased, i.e. whether a change or feature is "substantial".
If it is, the PR needs to update `version-properties` to the next minor version.
Note that the feature's Javadoc needs to reference the same version in its `@since` tag.

### Background

Like [Mockito](http://mockito.org/), JUnit Pioneer used Shipkit for a continuous delivery model, where every change on the `master` branch (for example when merging a pull request) triggered a release build that published a new version if the following criteria were met:

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
2. files in the repository (like [`README.md`](README.md) and and this `CONTRIBUTING.md`)
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
