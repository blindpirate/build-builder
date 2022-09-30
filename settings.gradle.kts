plugins {
    id("com.gradle.enterprise") version ("3.11.1")
}

rootProject.name = "build-builder"

val isCI = System.getenv().containsKey("CI")

if (isCI) {
    gradleEnterprise {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
