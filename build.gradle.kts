plugins {
    java
    jacoco
    checkstyle
    `maven-publish`
    id("com.diffplug.gradle.spotless") version "3.27.1"
    id("org.shipkit.java") version "2.3.1"
    id("at.zierler.yamlvalidator") version "1.5.0"
    id("org.sonarqube") version "2.8"
}

group = "org.junit-pioneer"
description = "JUnit 5 Extension Pack"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.4.2")
    implementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = "5.4.2")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.4.2")
    testImplementation(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.4.2")
    testImplementation(group = "org.junit.platform", name = "junit-platform-testkit", version = "1.4.2")

    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.15.0")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "3.3.3")
    testImplementation(group = "com.google.jimfs", name = "jimfs", version = "1.1")

    testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.12.1")
    testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-jul", version = "2.12.1")
}

spotless {
    val headerFile = file(".infra/spotless/eclipse-public-license-2.0.java")

    java {
        licenseHeaderFile(headerFile, "(package|import) ")
        importOrderFile(file(".infra/eclipse/junit-eclipse.importorder"))
        eclipse().configFile(".infra/eclipse/junit-eclipse-formatter-settings.xml")

        trimTrailingWhitespace()
        endWithNewline()
        removeUnusedImports()
    }

    format("groovy") {
        target("**/*.groovy")
        indentWithTabs()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeaderFile(headerFile, "package ")

        replaceRegex("class-level Javadoc indentation fix", """^\*""", " *")
        replaceRegex("nested Javadoc indentation fix", "\t\\*", "\t *")
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

tasks {

    test {
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
        shouldRunAfter(test)
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco/report.xml")
        }
    }

    check {
        // to find Javadoc errors early, let "javadoc" task run during "check"
        dependsOn(javadoc, validateYaml)
    }

    // the manifest needs to declare the future module name
    jar {
        manifest {
            attributes(
                    "Automatic-Module-Name" to "org.junitpioneer"
            )
        }
    }

    withType<Jar>().configureEach {
        from(projectDir) {
            include("LICENSE.md")
            into("META-INF")
        }
    }
}
