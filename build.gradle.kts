plugins {
    id("java")
    id("groovy")
    id("application")
    kotlin("jvm") version "1.7.20"
    id("org.gradle.test-retry") version "1.4.1"
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.airlift:airline:0.9")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.1.202206130422-r")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.1")

    testImplementation(gradleTestKit())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

application {
    mainClass.set("org.gradle.builds.Main")
}

val isCI = System.getenv().containsKey("CI")

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock()
            targets {
                all {
                    testTask {
                        maxParallelForks = if(isCI) 1 else 2
                        systemProperty("skipTestCleanup", System.getProperty("skipTestCleanup"))
                        systemProperty("user.name", "BuildBuilderTestUser")
                        systemProperty("user.home", layout.buildDirectory.dir("tmp/tests/user-home").get().asFile.canonicalPath)
                        retry {
                            maxRetries.set(if (isCI) 1 else 0)
                            maxFailures.set(20)
                            // TODO set this to `true` once less flaky
                            failOnPassedAfterRetry.set(!isCI)
                        }
                    }
                }
            }
        }
    }
}
