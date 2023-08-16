plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "cat.kiwi"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("cat.kiwi.clrwalk.CLRWalk")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}