group = "com.papaya.design.platform.bot.image"

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
