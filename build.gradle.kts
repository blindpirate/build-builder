plugins {
    id("java")
    id("groovy")
    id("application")
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.airlift:airline:0.7")
    implementation("org.eclipse.jgit:org.eclipse.jgit:4.9.1.201712030800-r")
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock()
            targets {
                all {
                    testTask.configure {
                        maxParallelForks = 2
                    }
                }
            }
        }
    }
}
