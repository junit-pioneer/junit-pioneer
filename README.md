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
It does not limit itself to proven ideas with wide application but is purposely open to experiments.
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

JUnit Pioneer is compiled against Java 8 (built with JDK 11), but comes as a module (i.e. with a `module-info.class`) named _org.junitpioneer_.
That means it can be used on all Java versions 8 and higher on class path and module path.

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
* If you want to help but don't know how, have a look at [the existing issues](https://github.com/junit-pioneer/junit-pioneer/issues), particularly [unassigned ones](https://github.com/junit-pioneer/junit-pioneer/issues?q=is%3Aopen+is%3Aissue+no%3Aassignee) and those [marked as up for grabs](https://github.com/junit-pioneer/junit-pioneer/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs).
* If you want to have a chat about JUnit Pioneer, [join our discord](https://discord.gg/rHfJeCF) - we have a _#junit-pioneer_ channel. 😊

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
	<dd>Java enthusiast with a passion for learning and sharing.
		He does that in blog posts, articles, newsletters, and books; in <a href="https://twitter.com/nipafx">tweets</a>, repos, videos, and streams; at conferences and in-house trainings - more on all of that on <a href="https://nipafx.dev">nipafx.dev</a>.
		That aside, he's best known for his head decor.
		He co-founded the project in November 2016 and has maintained it ever since (although often very negligently).</dd>
	<dt>Simon Schrottner aka <a href="https://github.com/aepfli">aepfli</a></dt>
	<dd>Bearded guy in Lederhosen, who loves to code, and loves to explore code quality, testing, and other tools that can improve the live of a software craftsman.
		<a href="https://www.couchsurfing.com/people/simmens">Passionated couchsurfer</a> and <a href="https://www.facebook.com/togtrama">hobby event planner</a>.
		Maintainer since April 2020.</dd>
</dl>

### Contributors

JUnit Pioneer, as small as it is, would be much smaller without kind souls contributing their time, energy, and skills.
Thank you for your efforts! 🙏

The least we can do is to thank them and list some of their accomplishments here (in lexicographic order).

#### 2022

* [Filip Hrisafov](https://github.com/filiphr) contributed the [JSON Argument Source](https://junit-pioneer.org/docs/json-argument-source/) support (#101 / #492)
* [Marcono1234](https://github.com/Marcono1234) contributed the [`@ExpectedToFail` extension](https://junit-pioneer.org/docs/expected-to-fail-tests/) (#551 / #676)
* [Mathieu Fortin](https://github.com/mathieufortin01) contributed the `suspendForMs` attribute in [retrying tests](https://junit-pioneer.org/docs/retrying-test/) (#407 / #604)
* [Pankaj Kumar](https://github.com/p1729) contributed towards improving GitHub actions (#587 / #611)
* [Rob Spoor](https://github.com/robtimus) enabled non-static factory methods for `@CartesianTest.MethodFactory` (#628)

#### 2021

* [Cory Thomas](https://github.com/dump247) contributed the `minSuccess` attribute in [retrying tests](https://junit-pioneer.org/docs/retrying-test/) (#408 / #430)
* [Daniel Kraus](https://github.com/beatngu13) fixed bugs in the environment variable and system property extensions (#432 / #433, #448 / #449, and more), revamped their annotation handling (#460 / #485), and improved the build process (#482 / #483) before becoming a maintainer
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
