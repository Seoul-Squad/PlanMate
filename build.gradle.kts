plugins {
    kotlin("jvm") version "2.1.20"
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
    testImplementation(kotlin("test"))
    // tests
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.1")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
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
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}