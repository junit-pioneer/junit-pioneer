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

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testImplementation(group = "org.junit.platform", name = "junit-platform-testkit")

    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.20.2")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "3.12.4")
    testImplementation(group = "com.google.jimfs", name = "jimfs", version = "1.2")

    testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
    testRuntimeOnly(group = "org.apache.logging.log4j", name = "log4j-jul", version = "2.14.1")

    implementation(rootProject)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
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
    compileTestJava {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        filter {
            includeTestsMatching("*Tests")
        }
        systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
    }
}