group = "com.papaya.design.platform.bot.image"

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":ai"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.springframework.data:spring-data-jpa")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}