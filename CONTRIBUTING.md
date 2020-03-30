# Contributing

The following guidelines were chosen very deliberately to make sure the project benefits from contributions.
This is true for such diverse areas as a firm legal foundation or a sensible and helpful commit history.

* [Contributor License Agreement](#junit-pioneer-contributor-license-agreement)
* [Fixing Bugs, Developing Features](#fixing-bugs-developing-features)
	* [Branching Strategy](#branching-strategy)
	* [Commits](#commits)
	* [Pull Requests](#pull-requests)
	* [Merging](#merging)
* [Updating Dependency on JUnit 5](#updating-dependency-on-junit-5)
* [Publishing](#publishing)
	* [Versioning](#versioning)

The guidelines apply to maintainers as well as contributors!


## JUnit Pioneer Contributor License Agreement

**Project License:** [Eclipse Public License v2.0](LICENSE.md)

* You will only submit contributions where you have authored 100% of the content.
* You will only submit contributions to which you have the necessary rights.
  This means that if you are employed you have received the necessary permissions from your employer to make the contributions.
* Whatever content you contribute will be provided under the project license(s).


## Fixing Bugs, Developing Features

This section governs how features or bug fixes are developed.
See the next section for how to adapt to upstream changes.

### Branching Strategy

By default, development happens in branches, which are merged via pull requests.
Special cases, like fixing problems with the CI pipeline, are of course exempt from this guideline.

Please make sure to give branches a meaningful name!
As an example, the one creating this documentation was called `branching-merging-documentation`.

### Documentation

Each feature is documented on [the project website](https://junit-pioneer.org/docs/), which is pulled from the files in the `docs/` folder, where each feature has:

* an entry in `docs-nav.yml` (lexicographically ordered)
* it's own `.adoc` file

Add these entries when implementing a new feature and update them when changing an existing one.

For information on how to use AsciiDoctor, check its [user manual](https://asciidoctor.org/docs/user-manual/) and [writer's guide](https://asciidoctor.org/docs/asciidoc-writers-guide/).
One project-specific requirement:

* one sentence per line, i.e. no matter how short or long a sentence is, it will occupy a single line, not shared with any other sentences
* to start a new paragraph, add a single blank line

Finally, do **not** update the `release-notes.md` file!
This file is generated automatically.

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

A pull request is accepted by squashing the commits and fast-forwarding master, making each bug fix or feature appear atomically on master.
This can be achieved with GitHub's [_squash and merge_](https://help.github.com/articles/about-pull-request-merges/#squash-and-merge-your-pull-request-commits) feature.

To make the single commit expressive its message must be detailed and [good]((http://chris.beams.io/posts/git-commit/)) (really, read that post!).
Furthermore, it must follow this structure:

```
${action}

${body}

${references}: ${issues}
PR: ${pull-request}
```

`${action}` should succinctly describe what the PR does in good Git style.
Ideally, this title line should not exceed 50 characters - 70 is the absolute maximum.

`${body}` should outline the problem the pull request was solving - it should focus on _why_ the code was written, not on _how_ it works.
This can usually be a summary of the issue description and discussion as well as commit messages.
Markdown syntax can be used and lines should usually not exceed 70 characters (exceptions are possible, e.g. to include stack traces).

The message ends with a list of related issues and the PR that merges the change:

* `${references}` is usually _Closes_, _Fixes_, or _Resolves_, but if none of that is the case, can also be _Issue(s)_
* `${issues}` is a comma-separated list of all related issues
* `${pull-request}` is the pull request

This makes the related issues and pull request easy to find from a look at the log.

Once a pull request is ready to be merged, the contributor will be asked to propose an action and body for the squashed commit and the maintainer will refine them when merging.

As an example, the squashed commit 22996a2, which created this documentation, should have had the following message:

```
Document branching and merging

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

Closes: #30, #31
PR: #40
```

(The actual message is slightly different because the guideline for location of the issue and pull request numbers was later changed and the example above was updated to reflect that.)

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

Like [Mockito](http://mockito.org/), JUnit Pioneer implements a continuous delivery model using [Shipkit](http://shipkit.org/) and [Travis CI](https://travis-ci.org/).
Every change on the `master` branch (for example when merging a pull request) triggers a release build that publishes a new version if the following criteria are met:

- the commit message doesn't contain `[ci skip-release]`
- all checks (e.g. tests) are successful
- at least one main artifact (that includes `...-source.jar` and `...-javadoc.jar`) has changed

Every new version is published to the `junit-pioneer/maven` Bintray repository as well as to Maven Central and JCenter.

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
