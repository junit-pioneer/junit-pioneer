# JUnit Pioneer

[![Travis build status](https://api.travis-ci.org/junit-pioneer/junit-pioneer.svg?branch=master)](https://travis-ci.org/junit-pioneer/junit-pioneer)
[![AppVeyor build status](https://ci.appveyor.com/api/projects/status/ijrlfaa2fpnxwm3r?svg=true)](https://ci.appveyor.com/project/nicolaiparlog/junit-pioneer)

A melting pot for all kinds of extensions to
[JUnit 5](https://github.com/junit-team/junit5), particular to its Jupiter API.

***

🚧 Much of this project is still under construction. 🚧

***

## Contributing

We welcome contributions of all kinds and shapes!
We are still building up infrastructure and what exactly we need help on is not easy to say, but we already settled on [what we want contributions to look like](CONTRIBUTING.md).

## Setup

### Publishing Snapshots

To publish snapshots to Maven Central you need to execute `gradle publish` after defining the properties `mavenUserName` and `mavenPassword`.

One way to do the latter are [Gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties).
For that approach, create a file `gradle.properties` in `GRADLE_USER_HOME` (which defaults to `USER_HOME/.gradle`) with the following content:

```
mavenUserName=...
mavenPassword=...
```

Another way are command line flags (but note that these add sensitive information to your terminal history):

```
gradle publish -PmavenUserName=--- -PmavenPassword=---
```
