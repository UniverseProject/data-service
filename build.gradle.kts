plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    // Allows to retrieve code coverage
    jacoco
}

val projectVersion: String by project

group = "org.universe"
version = projectVersion

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

val koinVersion: String by project
val ktorVersion: String by project
val ktSerializationVersion: String by project
val exposedVersion: String by project
val cacheVersion: String by project
val loggingVersion: String by project
val slf4jVersion: String by project
val mockkVersion: String by project
val junitVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit5"))

    // Koin for inject instance
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion") {
        // Problem with koin and junit
        // There is a conflict between the dependencies of both
        // So the solution is : Exclude the junit dependencies from one
        exclude("org.jetbrains.kotlin", "kotlin-test-junit")
    }

    // Ktor to interact with external API through HTTP
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Kotlin Serialization to serialize data for database and cache
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerializationVersion")

    // Exposed to interact with the SQL database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // Cache with map (for local without real service) or redis
    implementation("dev.kord.cache:cache-api:$cacheVersion")
    implementation("dev.kord.cache:cache-map:$cacheVersion")
    implementation("dev.kord.cache:cache-redis:$cacheVersion")

    // Logging information
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    // Create fake instance (mock) of components for tests
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Junit to run tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
}

tasks {
    jacocoTestReport {
        dependsOn(test) // tests are required to run before generating the report
        reports {
            xml.required.set(true)
        }
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport) // report is always generated after tests run
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"

        kotlinOptions.freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-Xopt-in=kotlin.time.ExperimentalTime"
        )
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("${project.name}.jar")
        destinationDirectory.set(file("build"))
    }
}