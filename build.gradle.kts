plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
}

repositories {
    mavenCentral()
}

dependencies {

    testImplementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}