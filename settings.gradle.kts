rootProject.name = "junit-pioneer"

plugins {
	id("com.gradle.enterprise") version "3.16.2"
}

gradleEnterprise { 
	buildScan {
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}

// See https://community.sonarsource.com/t/sonar-gradle-skipcompile-is-not-working/102710/9
// TODO move this to a better place once https://sonarsource.atlassian.net/browse/SONARGRADL-134 is resolved.
// > The 'sonar' / 'sonarqube' task depends on compile tasks.
// > This behavior is now deprecated and will be removed in version 5.x.
// > To avoid implicit compilation, set property 'sonar.gradle.skipCompile' to 'true' and make sure your project is compiled, before analysis has started.
System.setProperty("sonar.gradle.skipCompile", "true")
