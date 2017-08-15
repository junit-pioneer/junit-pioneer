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

## Project Structure

### Dependencies

To not add to user's [JAR hell](https://blog.codefx.org/java/jar-hell/), JUnit Pioneer is not taking on any runtime dependencies besides JUnit 5.
For our own infrastructure, we rely on the following compile and test dependencies:

* JSR-305 (for static analysis)
* AssertJ (for our tests)
* Mockito (for our tests)
