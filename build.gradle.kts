plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.papaya.design.platform"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}


subprojects {
    apply(plugin = "io.spring.dependency-management")

    extra["springAiVersion"] = "1.0.1"

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
