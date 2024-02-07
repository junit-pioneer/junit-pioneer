rootProject.name = "junit-pioneer"

plugins {
	id("com.gradle.enterprise") version "3.16.2"
	id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

gradleEnterprise { 
	buildScan {
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}

// TODO move this to a better place once https://sonarsource.atlassian.net/browse/SONARGRADL-134 is resolved.
// > The 'sonar' / 'sonarqube' task depends on compile tasks.
// > This behavior is now deprecated and will be removed in version 5.x.
// > To avoid implicit compilation, set property 'sonar.gradle.skipCompile' to 'true' and make sure your project is compiled, before analysis has started.
System.setProperty("sonar.gradle.skipCompile", "true")
