plugins {
	java
	jacoco
	checkstyle
	`maven-publish`
	signing
	id("com.diffplug.spotless") version "6.18.0"
	id("at.zierler.yamlvalidator") version "1.5.0"
	id("org.sonarqube") version "4.0.0.2929"
	id("org.shipkit.shipkit-changelog") version "1.2.0"
	id("org.shipkit.shipkit-github-release") version "1.2.0"
	id("com.github.ben-manes.versions") version "0.46.0"
	id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
	id("org.gradlex.extra-java-module-info") version "1.3"
}

plugins.withType<JavaPlugin>().configureEach {
	configure<JavaPluginExtension> {
		modularity.inferModulePath.set(true)
	}
}

group = "org.junit-pioneer"
description = "JUnit 5 Extension Pack"

val experimentalJavaVersion : String? by project
val experimentalBuild: Boolean = experimentalJavaVersion?.isNotEmpty() ?: false
val releaseBuild : Boolean = project.version != "unspecified"

val targetJavaVersion = JavaVersion.VERSION_11

java {
	if (experimentalBuild) {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(experimentalJavaVersion!!))
		}
	} else {
		sourceCompatibility = targetJavaVersion
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

val junitVersion : String by project
val jacksonVersion: String = "2.14.2"
val assertjVersion: String = "3.24.2"
val log4jVersion: String = "2.20.0"
val jimfsVersion: String = "1.2"

dependencies {
	implementation(platform("org.junit:junit-bom:$junitVersion"))

	implementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
	implementation(group = "org.junit.jupiter", name = "junit-jupiter-params")
	implementation(group = "org.junit.platform", name = "junit-platform-launcher")
	"jacksonImplementation"(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion)

	testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine")
	testImplementation(group = "org.junit.platform", name = "junit-platform-testkit")

	testImplementation(group = "org.assertj", name = "assertj-core", version = assertjVersion)
	testImplementation(group = "org.mockito", name = "mockito-core", version = "4.11.0")
	testImplementation(group = "com.google.jimfs", name = "jimfs", version = jimfsVersion)
	testImplementation(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "3.14.1")

	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-jul", version = log4jVersion)
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
	toolVersion = "10.12.3"
	configDirectory.set(rootProject.file(".infra/checkstyle"))
}

yamlValidator {
	searchPaths = listOf("docs")
	isSearchRecursive = true
}

jacoco {
	toolVersion = "0.8.9"
}

sonar {
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
	isRequired = releaseBuild && gradle.taskGraph.hasTask("publishToSonatype")
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

extraJavaModuleInfo {
	failOnMissingModuleInfo.set(false)
	automaticModule("com.google.guava:failureaccess", "com.google.guava.failureaccess")
	automaticModule("com.google.guava:listenablefuture", "com.google.guava.listenablefuture")
	automaticModule("com.google.code.findbugs:jsr305", "com.google.code.findbugs.jsr305")
	automaticModule("com.google.j2objc:j2objc-annotations", "com.google.j2objc.annotations")
	automaticModule("com.google.jimfs:jimfs", "com.google.jimfs")
}

tasks {

	sourceSets {
		create("demo") {
			java {
				srcDir("src/demo/java")
			}
			compileClasspath += sourceSets.main.get().output
			runtimeClasspath += sourceSets.main.get().output
		}
	}
	project(":demo") {
		sonar {
			isSkipProject = true
		}
	}
	// Adds all dependencies of main to demo sourceSet
	configurations["demoImplementation"].extendsFrom(configurations.testImplementation.get())
	// Ensures JUnit 5 engine is available to demo at runtime
	configurations["demoRuntimeOnly"].extendsFrom(configurations.testImplementation.get())

	compileJava {
		options.encoding = "UTF-8"
		options.compilerArgs.add("-Werror")
		// Do not break the build on "exports" warnings (see CONTRIBUTING.md for details)
		options.compilerArgs.add("-Xlint:all,-exports")

		if (project.version != "unspecified") {
			// Add version to Java modules
			options.javaModuleVersion.set(project.version.toString());
		}
	}

	// Prepares test-related JVM args
	val moduleName = "org.junitpioneer"
	// See https://docs.gradle.org/current/userguide/java_testing.html#sec:java_testing_modular_patching
	val patchModuleArg = "--patch-module=$moduleName=${compileJava.get().destinationDirectory.asFile.get().path}"
	val testJvmArgs = listOf(
			// EnvironmentVariableUtils: make java.util.Map accessible
			"--add-opens=java.base/java.util=$moduleName",
			// EnvironmentVariableUtils: make java.lang.System accessible
			"--add-opens=java.base/java.lang=$moduleName",
			patchModuleArg
	)

	compileTestJava {
		options.encoding = "UTF-8"
		options.compilerArgs.add("-Werror")
		options.compilerArgs.add(patchModuleArg)
		var xlintArg = "-Xlint:all"
		xlintArg += ",-exports,-requires-automatic"
		// missing-explicit-ctor was added in Java 16. This causes errors on test classes, which don't have one.
		if (JavaVersion.current() >= JavaVersion.VERSION_16) {
			xlintArg += ",-missing-explicit-ctor"
		}
		options.compilerArgs.add(xlintArg)
	}

	test {
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
		// java.security.manager was added in Java 12 (see
		// https://www.oracle.com/java/technologies/javase/12-relnote-issues.html#JDK-8191053). We have to explicitly
		// set it to "allow" for EnvironmentVariableUtilsTests$With_SecurityManager.
		if (JavaVersion.current() >= JavaVersion.VERSION_12)
			systemProperty("java.security.manager", "allow")
		// Disables Byte Buddy validation for the maximum supported class file version, since we are possibly using a
		// Java EA release.
		if (experimentalBuild)
			systemProperty("net.bytebuddy.experimental", true)
		jvmArgs(testJvmArgs)
	}

	testing {
		suites {
			val test by getting(JvmTestSuite::class) {
				useJUnitJupiter()
			}

			val demoTests by registering(JvmTestSuite::class) {
				dependencies {
					implementation(project(project.path))
					implementation("com.google.jimfs:jimfs:$jimfsVersion")
					implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
					implementation("org.assertj:assertj-core:$assertjVersion")
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
		}
	}

	javadoc {
		if (releaseBuild) {
			javadocTool.set(project.javaToolchains.javadocToolFor {
				// create Javadoc with least Java version to get all features
				// (e.g. search result page on 20)
				languageVersion.set(JavaLanguageVersion.of(20))
			})
		}

		options {
			// Cast to standard doclet options, see https://github.com/gradle/gradle/issues/7038#issuecomment-448294937
			this as StandardJavadocDocletOptions

			encoding = "UTF-8"
			links = listOf("https://junit.org/junit5/docs/current/api/")

			// Set javadoc `--release` flag (affects which warnings and errors are reported)
			// (Note: Gradle adds one leading '-' to the option on its own)
			// Have to use at least Java 9 to support modular build
			addStringOption("-release", targetJavaVersion.majorVersion.toInt().toString())

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
