plugins {
    groovy
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.8.1")
    implementation(group = "org.ajoberstar", name = "grgit", version = "1.9.3")
}
