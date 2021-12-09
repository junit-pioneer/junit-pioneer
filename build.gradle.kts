plugins {
	java
	jacoco
	checkstyle
	`maven-publish`
	signing
	id("com.diffplug.spotless") version "5.14.3"
	id("at.zierler.yamlvalidator") version "1.5.0"
	id("org.sonarqube") version "3.3"
	id("org.moditect.gradleplugin") version "1.0.0-rc3"
	id("org.shipkit.shipkit-changelog") version "1.1.15"
	id("org.shipkit.shipkit-github-release") version "1.1.15"
	id("com.github.ben-manes.versions") version "0.39.0"
	id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

plugins.withType<JavaPlugin>().configureEach {
	configure<JavaPluginExtension> {
		modularity.inferModulePath.set(true)
	}
}

group = "org.junit-pioneer"
description = "JUnit 5 Extension Pack"

val modularBuild : String by project
val experimentalJavaVersion : String? by project
val experimentalBuild: Boolean = experimentalJavaVersion?.isNotEmpty() ?: false

java {
	if (experimentalBuild) {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(experimentalJavaVersion!!))
		}
	} else {
		sourceCompatibility = if (modularBuild.toBoolean()) {
			JavaVersion.VERSION_11
		} else {
			JavaVersion.VERSION_1_8
		}
	}
	withJavadocJar()
	withSourcesJar()
}

repositories {
	mavenCentral()
}

val junitVersion : String by project

dependencies {
	implementation(platform("org.junit:junit-bom:$junitVersion"))

	implementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
	implementation(group = "org.junit.jupiter", name = "junit-jupiter-params")
	implementation(group = "org.junit.platform", name = "junit-platform-commons")
	implementation(group = "org.junit.platform", name = "junit-platform-launcher")

	testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine")
	testImplementation(group = "org.junit.platform", name = "junit-platform-testkit")

	testImplementation(group = "org.assertj", name = "assertj-core", version = "3.20.2")
	testImplementation(group = "org.mockito", name = "mockito-core", version = "3.12.4")
	testImplementation(group = "com.google.jimfs", name = "jimfs", version = "1.2")
	testImplementation(group = "nl.jqno.equalsverifier", name = "equalsverifier", version = "3.7.1")

	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
	testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-jul", version = "2.14.1")
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
	toolVersion = "7.8.2"
	configDirectory.set(rootProject.file(".infra/checkstyle"))
}

yamlValidator {
	searchPaths = listOf("docs")
	isSearchRecursive = true
}

jacoco {
	toolVersion = "0.8.6"
}

sonarqube {
	// If you want to use this logcally a sonarLogin has to be provide, either via Username and Password
	// or via token, https://docs.sonarqube.org/latest/analysis/analysis-parameters/
	properties {
		// Default properties if somebody wants to execute it locally
		property("sonar.projectKey", "junit-pioneer_junit-pioneer") // needs to be changed
		property("sonar.organization", "junit-pioneer-xp") // needs to be changed
		property("sonar.host.url", "https://sonarcloud.io")
	}
}

moditect {
	addMainModuleInfo {
		version = project.version
		overwriteExistingFiles.set(true)
		module {
			moduleInfoFile = rootProject.file("src/main/module/module-info.java")
		}
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
						"smoyer64" to "Steve Moyer",
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

tasks {

	sourceSets {
		main {
			if (modularBuild.toBoolean())
				java.srcDir("src/main/module")
		}
	}

	compileJava {
		options.encoding = "UTF-8"
	}

	compileTestJava {
		options.encoding = "UTF-8"
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
		// `EnvironmentVariableExtension` uses reflection to change environment variables;
		// this prevents the corresponding warning (and keeps working on Java 8)
		// IF YOU ADD MORE OPTIONS; CONSIDER REPLACING `-XX:+IgnoreUnrecognizedVMOptions WITH A CONDITIONAL
		jvmArgs(
				"-XX:+IgnoreUnrecognizedVMOptions",
				"--add-opens=java.base/java.util=ALL-UNNAMED")
	}

	javadoc {
		// of all the javadoc checks (accessibility, html, missing, reference, syntax; see
		// https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javadoc.html#BEJEFABE)
		// disable the warning for missing comments and tags because they spam the output
		// (it does often not make sense to comment every tag; e.g. the @return tag on annotations)
		(options as CoreJavadocOptions).addStringOption("Xdoclint:accessibility,html,syntax,reference", "-quiet")
		options.encoding = "UTF-8"
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
		dependsOn(javadoc, validateYaml)
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
