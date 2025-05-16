plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "1.9.21"
    jacoco
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // MongoDB Coroutines Driver
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
//    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.4")

    // MongoDB Reactive Streams Driver
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.10.0")

    // RxJava adapter for Reactive Streams
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    // tests
    testImplementation(kotlin("test"))
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.1")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

jacoco {
    toolVersion = "0.8.11" // latest stable
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // run tests first
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/default/**",
                        "**/di/**",
                        "**/org/example/di/**",
                        "**/presentation/utils/**",
                        "/org/example/presentation/MainUiController",
                        "**/org/example/presentation/navigation/**",
                        "**/org/example/data/source/local/csv/utils/command/**",
                        "**/data/source/remote/mongo/utils/**",
                        "**/org/example/presentation/role/**",
                        "**/org/example/presentation/**",
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.40".toBigDecimal()
            }
        }
    }
}
