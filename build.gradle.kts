plugins {
	java
	jacoco
	checkstyle
	`maven-publish`
	signing
	`jvm-test-suite`
	id("com.diffplug.spotless") version "6.4.2"
	id("at.zierler.yamlvalidator") version "1.5.0"
	id("org.sonarqube") version "3.3"
	id("org.shipkit.shipkit-changelog") version "1.1.15"
	id("org.shipkit.shipkit-github-release") version "1.1.15"
	id("com.github.ben-manes.versions") version "0.42.0"
	id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

plugins.withType<JavaPlugin>().configureEach {
	configure<JavaPluginExtension> {
		modularity.inferModulePath.set(true)
	}
}

group = "org.junit-pioneer"
description = "JUnit 5 Extension Pack"

val experimentalJavaVersion: String? by project
val experimentalBuild: Boolean = experimentalJavaVersion?.isNotEmpty() ?: false

val supportedJUnitVersions: String by project
val targetJavaVersion = JavaVersion.VERSION_11

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.toString()))
	}
	withJavadocJar()
	withSourcesJar()
	registerFeature("jackson") {
		usingSourceSet(sourceSets["main"])
	}
}

repositories {
	mavenCentral()
}

val junitVersion: String by project
val jacksonVersion: String = "2.13.2.2"

val pioneerImplementation: List<Dependency> = listOf(
		project.dependencies.create(group = "org.junit.jupiter", name = "junit-jupiter-api"),
		project.dependencies.create(group = "org.junit.jupiter", name = "junit-jupiter-params"),
		project.dependencies.create(group = "org.junit.platform", name = "junit-platform-commons"),
		project.dependencies.create(group = "org.junit.platform", name = "junit-platform-launcher"),
)
val pioneerTestImplementation: List<Dependency> = listOf(
		project.dependencies.create(group = "org.junit.jupiter", name = "junit-jupiter-engine"),
		project.dependencies.create(group = "org.junit.platform", name = "junit-platform-testkit"),
		project.dependencies.create(group = "org.assertj", name = "assertj-core", version = "3.22.0"),
		project.dependencies.create(group = "org.mockito", name = "mockito-core", version = "4.4.0"),
		project.dependencies.create(group = "com.google.jimfs", name = "jimfs", version = "1.2"),
		project.dependencies.create(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "3.10"),
)

dependencies {
	implementation(platform("org.junit:junit-bom:$junitVersion"))
	"jacksonImplementation"(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion)
	pioneerImplementation.forEach {
		implementation(it)
	}
	pioneerTestImplementation.forEach {
		testImplementation(it)
	}

	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.17.2")
	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-jul", version = "2.17.2")
}

spotless {
	val headerFile = file(".infra/spotless/eclipse-public-license-2.0.java")

	java {
		licenseHeaderFile(headerFile, "(package|import) ")
		importOrderFile(file(".infra/eclipse/junit-eclipse.importorder"))
		eclipse().configFile(".infra/eclipse/junit-eclipse-formatter-settings.xml")

		trimTrailingWhitespace()
		endWithNewline()
	}
}

checkstyle {
	toolVersion = "10.2"
	configDirectory.set(rootProject.file(".infra/checkstyle"))
}

yamlValidator {
	searchPaths = listOf("docs")
	isSearchRecursive = true
}

sonarqube {
	// If you want to use this locally a sonarLogin has to be provided, either via Username and Password
	// or via token, https://docs.sonarqube.org/latest/analysis/analysis-parameters/
	properties {
		// Default properties if somebody wants to execute it locally
		property("sonar.projectKey", "junit-pioneer_junit-pioneer") // needs to be changed
		property("sonar.organization", "junit-pioneer-xp") // needs to be changed
		property("sonar.host.url", "https://sonarcloud.io")
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])

			// additional pom content
			pom {
				name.set(project.name)
				description.set(project.description)
				url.set("https://junit-pioneer.org/")

				licenses {
					license {
						name.set("Eclipse Public License v2.0")
						url.set("https://www.eclipse.org/legal/epl-v20.html")
					}
				}

				scm {
					url.set("https://github.com/junit-pioneer/junit-pioneer.git")
				}

				issueManagement {
					system.set("GitHub Issues")
					url.set("https://github.com/junit-pioneer/junit-pioneer/issues")
				}

				ciManagement {
					system.set("GitHub Actions")
					url.set("https://github.com/junit-pioneer/junit-pioneer/actions")
				}

				developers {
					mapOf(
						"nipafx" to "Nicolai Parlog",
						"Bukama" to "Matthias Bünger",
						"aepfli" to "Simon Schrottner",
						"Michael1993" to "Mihály Verhás",
						"beatngu13" to "Daniel Kraus"
					).forEach {
						developer {
							id.set(it.key)
							name.set(it.value)
							url.set("https://github.com/" + it.key)
						}
					}
				}
			}
		}
	}
}

signing {
	setRequired({
		project.version != "unspecified" && gradle.taskGraph.hasTask("publishToSonatype")
	})
	val signingKey: String? by project
	val signingPassword: String? by project
	useInMemoryPgpKeys(signingKey, signingPassword)
	sign(publishing.publications.findByName("maven"))
}

nexusPublishing {
	repositories {
		sonatype()
	}
}

// Our Task to call all our versionTests
val versionTest by tasks.creating

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter()

			targets {
				all {
					testTask.configure {
						configure<JacocoTaskExtension> {
							isEnabled = !experimentalBuild
						}
						testLogging {
							setExceptionFormat("full")
						}
						useJUnitPlatform()
						filter {
							includeTestsMatching("*Tests")
						}
						systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
						// `EnvironmentVariableExtension` uses reflection to change environment variables;
						// this prevents the corresponding warning (and keeps working on Java 8)
						// IF YOU ADD MORE OPTIONS; CONSIDER REPLACING `-XX:+IgnoreUnrecognizedVMOptions WITH A CONDITIONAL
						jvmArgs(
								"-XX:+IgnoreUnrecognizedVMOptions",
								"--add-opens=java.base/java.util=ALL-UNNAMED")

					}
				}
			}
		}

		val demoTests by registering(JvmTestSuite::class) {
			dependencies {
				implementation(project)

				pioneerTestImplementation.forEach {dependency ->
					implementation(dependency)
				}
				implementation(project.dependencies.create(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion))

			}

			sources {
				java {
					srcDir("src/demo/java")
				}
				resources {
					srcDir("src/demo/resources")
				}
			}
			targets {
				all {
					testTask.configure {
						shouldRunAfter(test)
						filter {
							includeTestsMatching("*Demo")
						}
					}
				}
			}
		}

		supportedJUnitVersions.split(",").filter { it != junitVersion }.forEach {
			val testSuite = register("testWithJUnit$it", JvmTestSuite::class) {
				useJUnitJupiter()

				dependencies {
					implementation(project)
					pioneerImplementation.forEach { dependency ->
						implementation(dependency)
					}
					pioneerTestImplementation.forEach {dependency ->
						implementation(dependency)
					}
					implementation(project.dependencies.platform("org.junit:junit-bom:$it"))
				}

				sources {
					java {
						srcDir("src/test/java")
					}
					resources {
						srcDir("src/test/resources")
					}
				}

				targets {
					all {
						testTask.configure {
							shouldRunAfter(test)

							filter {
								includeTestsMatching("*Tests")
							}
						}
					}
				}
			}

			versionTest.dependsOn(testSuite)
		}
	}
}

tasks {

	// All compile Tasks should now use UTF-8 not just JavaCompile
	withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
	}


	javadoc {
		javadocTool.set(project.javaToolchains.javadocToolFor {
			// Create Javadoc with newer JDK to get the latest features, e.g. search bar
			languageVersion.set(JavaLanguageVersion.of(17))
		})

		options {
			// Cast to standard doclet options, see https://github.com/gradle/gradle/issues/7038#issuecomment-448294937
			this as StandardJavadocDocletOptions

			encoding = "UTF-8"
			links = listOf("https://junit.org/junit5/docs/current/api/")

			// Set javadoc `--release` flag (affects which warnings and errors are reported)
			// (Note: Gradle adds one leading '-' to the option on its own)
			// Have to use at least Java 9 to support modular build
			addStringOption("-release", maxOf(11, targetJavaVersion.majorVersion.toInt()).toString())

			// Enable doclint, but ignore warnings for missing tags, see
			// https://docs.oracle.com/en/java/javase/17/docs/specs/man/javadoc.html#additional-options-provided-by-the-standard-doclet
			// The Gradle option methods are rather misleading, but a boolean `true` value just makes sure the flag
			// is passed to javadoc, see https://github.com/gradle/gradle/issues/2354
			addBooleanOption("Xdoclint:all,-missing", true)
		}

		shouldRunAfter(test)
	}

	jacocoTestReport {
		enabled = !experimentalBuild
		reports {
			xml.required.set(true)
			xml.outputLocation.set(file("${buildDir}/reports/jacoco/report.xml"))
		}
	}

	check {
		// to find Javadoc errors early, let "javadoc" task run during "check"
		dependsOn(javadoc, validateYaml, testing.suites.named("demoTests"))
	}

	withType<Jar>().configureEach {
		from(projectDir) {
			include("LICENSE.md")
			into("META-INF")
		}
	}

	generateChangelog {
		val gitFetchRecentTag = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
		val recentTag = gitFetchRecentTag.inputStream.bufferedReader().readText().trim()
		previousRevision = recentTag
		githubToken = System.getenv("GITHUB_TOKEN")
		repository = "junit-pioneer/junit-pioneer"
	}

	githubRelease {
		dependsOn(generateChangelog)
		val generateChangelogTask = generateChangelog.get()
		repository = generateChangelogTask.repository
		changelog = generateChangelogTask.outputFile
		githubToken = generateChangelogTask.githubToken
		newTagRevision = System.getenv("GITHUB_SHA")
	}
}
