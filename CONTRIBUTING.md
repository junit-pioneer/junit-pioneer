# Contributing

The following guidelines were chosen very deliberately to make sure the project benefits from contributions.
This is true for such diverse areas as a firm legal foundation or a sensible and helpful commit history.

The guidelines apply to maintainers as well as contributors!

## JUnit Pioneer Contributor License Agreement

**Project License:** [Eclipse Public License v1.0](LICENSE.md)

* You will only submit contributions where you have authored 100% of the content.
* You will only submit contributions to which you have the necessary rights.
  This means that if you are employed you have received the necessary permissions from your employer to make the contributions.
* Whatever content you contribute will be provided under the project license(s).


## Fixing Bugs, Developing Features

This section governs how features or bug fixes are developed.
See the next section for how to adapt to upstream changes.

### Branching Strategy

By default development happens in branches, which are merged via pull requests.
Special cases, like fixing problems with the CI pipeline, are of course exempt from this guideline.

Please make sure to give branches a meaningful name!
As an example, the one creating this documentation was called `branching-merging-documentation`.

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
<Action> (<issues> / <pull-request>)

<Body>
```

`<Action>` should succinctly describe what the PR does in good Git style.
It is followed by a comma-separated list of all related issues, a dash, and the pull request (to make all of them easy to find from a look at the log).
Ideally, this title line should not exceed 50 characters - 70 is the absolute maximum.

`<Body>` should outline the problem the pull request was solving - it should focus on _why_ the code was written not on _how_ it works.
This can usually be a summary of the issue description and discussion as well as commit messages.
Markdown syntax can be used and lines should usually not exceed 70 characters (exceptions are possible, e.g. to include stack traces).

Once a pull request is ready to be merged, the contributor will be asked to propose an action and body for the squashed commit and the maintainer will refine them when merging.

As an example, the squashed commit that created this documentation had the following message:

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
```


## Publishing

### Snapshots

To publish snapshots to Maven Central you need to execute `gradle publish` after defining the properties `mavenUserName` and `mavenPassword`.

One way to do the latter are [Gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties).
For that approach, create a file `gradle.properties` in `GRADLE_USER_HOME` (which defaults to `USER_HOME/.gradle`) with the following content:

```
mavenUserName=...
mavenPassword=...
```

Another way are command line flags (but note that these add sensitive information to your terminal history):

```
gradle publish -PmavenUserName=... -PmavenPassword=...
```
