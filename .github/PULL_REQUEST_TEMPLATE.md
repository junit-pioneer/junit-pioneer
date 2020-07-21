
---
**PR checklist**

The following checklist shall help the PR's author, the reviewers and maintainers to ensure the quality of this project.
It bases on our contributors guidelines, especially the ["writing code" section](https://github.com/junit-pioneer/junit-pioneer/blob/master/CONTRIBUTING.md#writing-code).
It shall help to check for completion of the listed points.
If a point does not apply to the PR changes, the entry can just be marked as done. 

Documentation (general)
 
* [ ] There is documentation (Javadoc and site documentation; added or updated)
* [ ] There are implementation information to describe _why_ non-obvious source code / solution got implemented
* [ ] Site documentation has its own `.adoc` file in the `docs` folder, e.g. `docs/report-entries.adoc`
* [ ] Only one sentence per line (especially in `.adoc` files)
* [ ] Javadoc uses informal style, while sites documentation uses formal style (see #265)

Documentation (new extension)
* [ ] The `docs/docs-nav.yml` navigation has an entry for the new extension
* [ ] The `package-info.java` contains information about the new extension

Code
* [ ] Code satisfies code style, naming conventions etc.
* [ ] Successful tests cover all changes
* [ ] There are checks which validate correct / false usage / configuration of a functionality
There are tests to verify those checks (see #164)
* [ ] Tests use [AssertJ](https://joel-costigliola.github.io/assertj/) or our own PioneerAssertions (which are based on AssertJ)

Contributing
* [ ] A prepared commit message exists
* [ ] The list of contributions inside `README.MD` mentions the new contribution.
Publishing the authors real name is optional! 

---

I hereby agree to the terms of the [JUnit Pioneer Contributor License Agreement](https://github.com/junit-pioneer/junit-pioneer/blob/master/CONTRIBUTING.md#junit-pioneer-contributor-license-agreement).


