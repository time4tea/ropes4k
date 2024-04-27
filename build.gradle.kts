plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
    id("io.morethan.jmhreport") version "0.9.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("io.strikt:strikt-core:0.34.1")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("Performance")
    }
}

// build.gradle.kts
benchmark {
    targets {
        register("test")
    }
    benchmark {
        configurations {
            register("single") {
                include(".*.\\.SearchSimpleBenchmark")
            }
        }
    }
}



// currently has to be run by hand after the benchmark,else it picks up previous run :-(
jmhReport {

    fun findMostRecentJmhReportIn(d: File): String? {
        return d.walkBottomUp()
            .filter { it.name == "test.json" }
            .sortedByDescending { it.lastModified() }
            .firstOrNull()
            ?.absolutePath
            ?.also {
                println("Selected JMH Report is $it")
            }
    }

    jmhResultPath = findMostRecentJmhReportIn(project.file("build/reports/benchmarks"))
    jmhReportOutput = project.file("build/reports/benchmarks").absolutePath
}
