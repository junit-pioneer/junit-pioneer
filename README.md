# JUnit Pioneer

<img src="docs/project-logo.jpg" align="right" width="150px"/>

[![Latest Junit Pioneer on Maven Central](https://maven-badges.herokuapp.com/maven-central/org.junit-pioneer/junit-pioneer/badge.svg?style=flat)](https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer)
[![Latest JUnit Pioneer Javadoc on javadoc.io](https://javadoc.io/badge2/org.junit-pioneer/junit-pioneer/javadoc.svg)](https://javadoc.io/doc/org.junit-pioneer/junit-pioneer)
[![JUnit Pioneer build status](https://github.com/junit-pioneer/junit-pioneer/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/junit-pioneer/junit-pioneer/actions/workflows/build.yml?branch=main)
[![Contributor Covenant Code of Conduct](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](code_of_conduct.md)

A melting pot for all kinds of extensions to
[JUnit 5](https://github.com/junit-team/junit5), particular to its Jupiter API.

Check out [junit-pioneer.org](https://junit-pioneer.org/), particularly [the documentation section](https://junit-pioneer.org/docs/).


## A Pioneer's Mission

JUnit Pioneer provides extensions for [JUnit 5](https://github.com/junit-team/junit5/) and its Jupiter API.
It does not limit itself to proven ideas with wide application but is purposely open to experimentation.
It aims to spin off successful and cohesive portions into sibling projects or back into the JUnit 5 code base.

To enable easy exchange of code with JUnit 5, JUnit Pioneer copies most of its infrastructure, from code style to build tool and configuration to continuous integration.


## Getting on Board

JUnit Pioneer is released on [GitHub](https://github.com/junit-pioneer/junit-pioneer/releases) and [Maven Central](https://search.maven.org/artifact/org.junit-pioneer/junit-pioneer). Coordinates:

* group ID: `org.junit-pioneer`
* artifact ID: `junit-pioneer`
* version: [![Latest Junit Pioneer on Maven Central](https://maven-badges.herokuapp.com/maven-central/org.junit-pioneer/junit-pioneer/badge.svg?style=flat)](https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer)

For Maven:

```xml
<dependency>
	<groupId>org.junit-pioneer</groupId>
	<artifactId>junit-pioneer</artifactId>
	<version><!--...--></version>
	<scope>test</scope>
</dependency>
```

For Gradle:

```groovy
testCompile group: 'org.junit-pioneer', name: 'junit-pioneer', version: /*...*/
```


## Dependencies

Starting with release 2.0, JUnit Pioneer is compiled against **Java 11** and comes as a module (i.e. with a `module-info.class`) named _org.junitpioneer_.
That means it can be used on all Java versions 11 and higher on class path and module path.

Pioneer does not only use JUnit 5's API, but also other artifacts from its ecosystem such as [`junit-platform-commons`](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-commons).
To avoid dependency issues (e.g. in [junit-pioneer#343](https://github.com/junit-pioneer/junit-pioneer/issues/343)), you should add the JUnit 5 BOM ([`junit-bom`](https://mvnrepository.com/artifact/org.junit/junit-bom)) to your project instead of defining all dependency versions manually.

To not add to user's [JAR hell](https://nipafx.dev/jar-hell/), JUnit Pioneer is not taking on any runtime dependencies besides JUnit 5.
Pioneer always depends on the lowest JUnit 5 version that supports its feature set, but that should not keep you from using 5's latest and greatest.

Since 1.7.0 we also have an **optional** runtime dependency on [Jackson](https://github.com/FasterXML/jackson), for our JSON-based extensions.
You can read a bit more about our approach to dependencies in the [contribution guide](CONTRIBUTING.md#others).

For our own infrastructure, we rely on the following compile and test dependencies:

* AssertJ (assertions for our tests)
* Mockito (mocking for our tests)
* Log4J (to configure logging during test runs)
* Jimfs (as an in-memory file system for our test)
* Equalsverifier (for easier assertion of simple things)


## Contributing

We welcome contributions of all shapes and forms! 🌞

* If you have an idea for an extension, [open an issue](https://github.com/junit-pioneer/junit-pioneer/issues/new) and let's discuss.
* If you want to help but don't know how, have a look at [the existing issues](https://github.com/junit-pioneer/junit-pioneer/issues), particularly those marked [_up for grabs_](https://github.com/junit-pioneer/junit-pioneer/labels/%F0%9F%93%A2%20up%20for%20grabs) or [_good first issue_](https://github.com/junit-pioneer/junit-pioneer/labels/good%20first%20issue).
* If you want to chat about JUnit Pioneer, [join our discord](https://discord.gg/rHfJeCF) - we have a _#junit-pioneer_ channel. 😊

Before contributing, please read the [contribution guide](CONTRIBUTING.md) as well as [the code of conduct](CODE_OF_CONDUCT.md).

### Maintainers

JUnit Pioneer is maintained by a small team of people who work on it in their free time - see [`CONTRIBUTING.md`](CONTRIBUTING.md) for details on how they do that.
In lexicographic order, these are:

<dl>
	<dt>Daniel Kraus aka <a href="https://github.com/beatngu13">beatngu13</a></dt>
	<dd>Banking software by day, OSS by night.
		Punk rock enthusiast and passionate hiker.
		Into Java, software testing, and web services.
		<a href="https://twitter.com/beatngu1101">Tweets</a> occasionally.</dd>
	<dt>Matthias Bünger aka <a href="https://github.com/Bukama">Bukama</a></dt>
	<dd>(Always tries to become a better) Java developer, loves testing and reads <a href="https://twitter.com/bukamabish">tweets</a>.
		Became a maintainer in April 2020 after he "caused" (authored) too many <a href="https://github.com/junit-pioneer/junit-pioneer/issues">bishues</a>.</dd>
	<dt>Mihály Verhás aka <a href="https://github.com/Michael1993">Michael1993</a></dt>
	<dd>Not so witty, not so pretty, not really mean, not really cool bean.
		A Hungarian Java developer who spends more time on Twitch than recommended by his doctors and used creative and diligent contributions to fool everyone into believing he is a decent enough guy to get promoted to maintainer (in November 2020).
		</dd>
	<dt>Nicolai Parlog aka <a href="https://github.com/nipafx">nipafx</a></dt>
	<dd>Java enthusiast with a passion for learning and sharing, best known for his head decor.
		He's a Java Developer Advocate at Oracle, organizer of <a href="https://accento.dev/">Accento</a>, occasional streamer, and more - check <a href="https://nipafx.dev">nipafx.dev</a> for the full list.
		He co-founded JUnit Pioneer in November 2016 and has maintained it ever since (although often very negligently).</dd>
	<dt>Simon Schrottner aka <a href="https://github.com/aepfli">aepfli</a></dt>
	<dd>Bearded guy in Lederhosen, who loves to code, and loves to explore code quality, testing, and other tools that can improve the live of a software craftsman.
		<a href="https://www.couchsurfing.com/people/simmens">Passionated couchsurfer</a> and <a href="https://www.facebook.com/togtrama">hobby event planner</a>.
		Maintainer since April 2020.</dd>
</dl>

### Contributors

JUnit Pioneer, as small as it is, would be much smaller without kind souls contributing their time, energy, and skills.
Thank you for your efforts! 🙏

The least we can do is to thank them and list some of their accomplishments here (in lexicographic order).

#### 2023
* [Eric Everman](https://github.com/eeverman) added `@RestoreSystemProperties` and `@RestoreEnvironmentVariables` annotations to the [System Properties](https://junit-pioneer.org/docs/system-properties/) and [Environment Variables](https://junit-pioneer.org/docs/environment-variables/) extensions (#574 / #700)

#### 2022

* [Filip Hrisafov](https://github.com/filiphr) contributed the [JSON Argument Source](https://junit-pioneer.org/docs/json-argument-source/) support (#101 / #492)
* [Marcono1234](https://github.com/Marcono1234) contributed the [`@ExpectedToFail` extension](https://junit-pioneer.org/docs/expected-to-fail-tests/) (#551 / #676)
* [Mathieu Fortin](https://github.com/mathieufortin01) contributed the `suspendForMs` attribute in [retrying tests](https://junit-pioneer.org/docs/retrying-test/) (#407 / #604)
* [Pankaj Kumar](https://github.com/p1729) contributed towards improving GitHub actions (#587 / #611)
* [Rob Spoor](https://github.com/robtimus) enabled non-static factory methods for `@CartesianTest.MethodFactory` (#628)
* [Marc Wrobel](https://github.com/marcwrobel) improved the documentation (#692)

#### 2021

* [Cory Thomas](https://github.com/dump247) contributed the `minSuccess` attribute in [retrying tests](https://junit-pioneer.org/docs/retrying-test/) (#408 / #430)
* [Daniel Kraus](https://github.com/beatngu13) fixed bugs in the environment variable and system property extensions (#432 / #433, #448 / #449, and more), revamped their annotation handling (#460 / #485), and improved the build process (#482 / #483) before becoming a maintainer
= Contributor Covenant Code of Conduct

Our Pledge
-----------

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, caste, color, religion, or sexual identity and orientation.

We pledge to act and interact in ways that contribute to an open, welcoming, diverse, inclusive, and healthy community.

Our Standards
--------------

Examples of behavior that contributes to a positive environment for our community include:

- Demonstrating empathy and kindness toward other people
- Being respectful of differing opinions, viewpoints, and experiences
- Giving and gracefully accepting constructive feedback
- Accepting responsibility and apologizing to those affected by our mistakes, and learning from the experience
- Focusing on what is best not just for us as individuals, but for the overall community

Examples of unacceptable behavior include:

- The use of sexualized language or imagery, and sexual attention or advances of any kind
- Trolling, insulting or derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information, such as a physical or email address, without their explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

Enforcement Responsibilities
-----------------------------

Community leaders are responsible for clarifying and enforcing our standards of acceptable behavior and will take appropriate and fair corrective action in response to any behavior that they deem inappropriate, threatening, offensive, or harmful.

Community leaders have the right and responsibility to remove, edit, or reject comments, commits, code, wiki edits, issues, and other contributions that are not aligned to this Code of Conduct, and will communicate reasons for moderation decisions when appropriate.

Scope
-----

This Code of Conduct applies within all community spaces (this includes but is not limited to the project's presence on GitHub and the nipafx Discord server), and also applies when an individual is officially representing the community in public spaces. Examples of representing our community include using an official e-mail address, posting via an official social media account, or acting as an appointed representative at an online or offline event.

Stark violations outside of immediate community spaces (for example on Twitter, even if not directed at community members) are also in scope of this Code of Conduct.

Enforcement
-----------

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported to Nicolai Parlog (contact options) or the other maintainers (via Discord DMs). All complaints will be reviewed and investigated fairly and as promptly as circumstances allow (as explained in the contribution guide, there's no expectation of availability towards maintainers).

All community leaders are obligated to respect the privacy and security of the reporter of any incident.

Enforcement Guidelines
----------------------

Community leaders will follow these Community Impact Guidelines in determining the consequences for any action they deem in violation of this Code of Conduct:

1. Correction

	Community Impact: Use of inappropriate language or other behavior deemed unprofessional or unwelcome in the community.

	Consequence: A private (if possible), written warning from community leaders, providing clarity around the nature of the violation and an explanation of why the behavior was inappropriate. A public apology may be requested.

2. Warning

	Community Impact: A violation through a single incident or series of actions.

	Consequence: A warning with consequences for continued behavior. No interaction with the people involved, including unsolicited interaction with those enforcing the Code of Conduct, for a specified period of time. This includes avoiding interactions in community spaces as well as external channels like social media. Violating these terms may lead to a temporary or permanent ban.

3. Temporary Ban

	Community Impact: A serious violation of community standards, including sustained inappropriate behavior.

	Consequence: A temporary ban from any sort of interaction or public communication with the community for a specified period of time. No public or private interaction with the people involved, including unsolicited interaction with those enforcing the Code of Conduct, is allowed during this period. Violating these terms may lead to a permanent ban.

4. Permanent Ban

	Community Impact: Demonstrating a pattern of violation of community standards, including sustained inappropriate behavior, harassment of an individual, or aggression toward or disparagement of classes of individuals.

	Consequence: A permanent ban from any sort of public interaction within the community.

Attribution
------------

This Code of Conduct is adapted from the Contributor Covenant, version 2.1, available at https://www.contributor-covenant.org/version/2/1/code_of_conduct.html.

Community Impact Guidelines were inspired by Mozilla's code of conduct enforcement ladder.

For answers to common questions about this code of conduct, see the FAQ at https://www.contributor-covenant.org/faq. Translations are available at https://www.contributor-covenant.org/translations.
* [Gabriel Diegel](https://github.com/gdiegel) contributed the `@DisabledUntil` extension in [Temporarily disable a test](https://junit-pioneer.org/docs/disabled-until/) (#366)
* [John Lehne](https://github.com/johnlehne) resolved an issue with the latest build status not showing correctly in README.md (#530)
* [Jonathan Bluett-Duncan](https://github.com/jbduncan) contributed a fix to `buildSrc/build.gradle` which was failing when a `.idea` directory did not contain a `vcs.xml` file (#532)
* [Scott Leberknight](https://github.com/sleberknight) resolved a javadoc issue (#547 / #548)
* [Slawomir Jaranowski](https://github.com/slawekjaranowski) Migrate to new Shipkit plugins (#410 / #419)
* [Stefano Cordio](https://github.com/scordio) contributed [the Cartesian Enum source](https://junit-pioneer.org/docs/cartesian-product/#cartesianenumsource) (#379 / #409 and #414 / #453)

#### 2020

* [Allon Murienik](https://github.com/mureinik) contributed [the range sources](https://junit-pioneer.org/docs/range-sources/) (#44 / #123)
* [Bradford Hovinen](https://github.com/hovinen) improved the execution of the EnvironmentVariableUtils on different OS (#287 / #288)
* [Daniel Kraus](https://github.com/beatngu13) contributed [the system property extension](https://junit-pioneer.org/docs/system-properties/) (#129 / #133) and further improved it, also worked on the environment variable extension (#180 / #248), the Cartesian product extension (#358 / #372), and helped with build infrastructure (e.g. #269)
* [David Walluck](https://github.com/dwalluck) introduced JUnit 5 BOM (#343 / #346)
* [Dirk Witzel](https://github.com/NPException) improved the documentation (#149 / #271)
* [Ignat Simonenko](https://github.com/simonenkoi) fixed a noteworthy bug in the default locale extension (#146 / #161)
* [Mark Rösler](https://github.com/Hancho2009) contributed the [environment variable extension](https://junit-pioneer.org/docs/environment-variables/) (#167 / #174 and #241 / #242)
* [Matthias Bünger](https://github.com/Bukama) opened, vetted, and groomed countless issues and PRs and contributed multiple refactorings (e.g. #165 / #168) and fixes (e.g. #190 / #200) before getting promoted to maintainer
* [Mihály Verhás](https://github.com/Michael1993) contributed [the StdIO extension](https://junit-pioneer.org/docs/standard-input-output/) (#34 / #227), [the ReportEntryExtension](https://junit-pioneer.org/docs/report-entries/) (#134, #179 / #183, #216, #294), [the CartesianProductTestExtension](https://junit-pioneer.org/docs/cartesian-product/) (#321, #362 / #68, #354), [the DisableIfParameterExtension](https://junit-pioneer.org/docs/disable-parameterized-tests/) (#313, #368) added tests to other extensions (#164 / #272), the Pioneer assertions and contributed to multiple issues (e.g. #217 / #298) and PRs (e.g. #253, #307)
* [Nishant Vashisth](https://github.com/nishantvas) contributed an [extension to disable parameterized tests](https://junit-pioneer.org/docs/disable-if-display-name/) by display name (#163 / #175)
* [Simon Schrottner](https://github.com/aepfli) contributed to multiple issues and PRs and almost single-handedly revamped the build and QA process (e.g. #192 / #185) before getting promoted to maintainer
* [Sullis](https://github.com/sullis) improved GitHub Actions with Gradle Wrapper Validation check (#302)

#### 2019

* [Alex Panchenko](https://github.com/panchenko) fixed a noteworthy bug in the `TempDirectory` extension (#140)
* [Christian Stein](https://github.com/sormuras) helped get the project back on track (yes, again, I told you Nicolai was negligent)
* [Daniel Kraus](https://github.com/beatngu13) improved Shipkit integration (#148 / #151)
* [Marc Philipp](https://github.com/marcphilipp) helped get the project back on track and converted `build.gradle` to Kotlin (#145)

#### 2018

* [Benedikt Ritter](https://github.com/britter) contributed [the default locale and time zone extensions](https://junit-pioneer.org/docs/default-locale-timezone/) (#103 / #104)
* [Christian Stein](https://github.com/sormuras) introduced Shipkit-based continuous delivery (#87) and build scans (#124 / #132)
* [Marc Philipp](https://github.com/marcphilipp) helped get the project back on track and contributed [the `TempDirectory` extension](https://junit-pioneer.org/docs/temp-directory/) (#39 / #69)
