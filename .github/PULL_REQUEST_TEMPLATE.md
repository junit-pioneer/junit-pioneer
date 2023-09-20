Proposed commit message:

```
${action} (${issues} / ${pull-request}) [max 70 characters]

${body} [max 70 characters per line]

${references}: ${issues}
PR: ${pull-request}
```

---
**PR checklist**

The following checklist shall help the PR's author, the reviewers and maintainers to ensure the quality of this project.
It is based on our contributors guidelines, especially the ["writing code" section](https://github.com/junit-pioneer/junit-pioneer/blob/main/CONTRIBUTING.adoc#writing-code).
It shall help to check for completion of the listed points.
If a point does not apply to the given PR's changes, the corresponding entry can be simply marked as done. 

Documentation (general)
* [ ] There is documentation (Javadoc and site documentation; added or updated)
* [ ] There is implementation information to describe _why_ a non-obvious source code / solution got implemented
* [ ] Site documentation has its own `.adoc` file in the `docs` folder, e.g. `docs/report-entries.adoc`
* [ ] Site documentation in `.adoc` file references demo in `src/demo/java` instead of containing code blocks as text
* [ ] Only one sentence per line (especially in `.adoc` files)
* [ ] Javadoc uses formal style, while sites documentation may use informal style

Documentation (new extension)
* [ ] The `docs/docs-nav.yml` navigation has an entry for the new extension
* [ ] The `package-info.java` contains information about the new extension

Code (general)
* [ ] Code adheres to code style, naming conventions etc.
* [ ] Successful tests cover all changes
* [ ] There are checks which validate correct / false usage / configuration of a functionality and there are tests to verify those checks
* [ ] Tests use [AssertJ](https://assertj.github.io/doc/) or our own [PioneerAssert](https://github.com/junit-pioneer/junit-pioneer/blob/main/CONTRIBUTING.adoc#assertions) (which are based on AssertJ)

Code (new package)
* [ ] The new package is exported in `module-info.java`
* [ ] The new package is also present in the tests
* [ ] The new package is opened for reflection to JUnit 5 in `module-info.java`
* [ ] The new package is listed in the contribution guide

Contributing
* [ ] A prepared commit message exists
* [ ] The list of contributions inside `README.adoc` mentions the new contribution (real name optional) 
