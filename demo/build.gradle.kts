plugins {
    java
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

val junitVersion : String by rootProject

dependencies {
    implementation(platform("org.junit:junit-bom:$junitVersion"))

    implementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
    implementation(group = "org.junit.jupiter", name = "junit-jupiter-params")
    implementation(group = "org.junit.platform", name = "junit-platform-commons")
    implementation(group = "org.junit.platform", name = "junit-platform-launcher")

    implementation(rootProject)
}

spotless {
    val headerFile = file("../.infra/spotless/eclipse-public-license-2.0.java")

    java {
        licenseHeaderFile(headerFile, "(package|import) ")
        importOrderFile(file("../.infra/eclipse/junit-eclipse.importorder"))
        eclipse().configFile("../.infra/eclipse/junit-eclipse-formatter-settings.xml", "demo-formatter-settings.xml")

        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks {
    test {
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