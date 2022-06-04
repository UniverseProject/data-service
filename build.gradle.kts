plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
    signing
}

group = Library.group
version = Library.version

buildscript {
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

val kotlinVersion: String by project
val kotlinCoroutineReactiveVersion: String by project
val koinVersion: String by project
val ktorVersion: String by project
val ktSerializationVersion: String by project
val exposedVersion: String by project
val cacheVersion: String by project
val loggingVersion: String by project
val slf4jVersion: String by project
val mockkVersion: String by project
val junitVersion: String by project
val junitPioneerVersion: String by project
val testContainersVersion: String by project
val psqlVersion: String by project
val konfVersion: String by project
val lettuceVersion: String by project
val apachePoolVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit5"))
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutineReactiveVersion")


    // Koin for inject instance
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion") {
        // Problem with koin and junit
        // There is a conflict between the dependencies of both
        // So the solution is : Exclude the junit dependencies
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$ktSerializationVersion")

    // Exposed to interact with the SQL database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:$psqlVersion")

    // Redis cache
    implementation("io.lettuce:lettuce-core:$lettuceVersion")
    implementation("org.apache.commons:commons-pool2:$apachePoolVersion")

    // Logging information
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    // Create fake instance (mock) of components for tests
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Junit to run tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
}

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.ExperimentalStdlibApi")
            }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    val dokkaOutputDir = "${rootProject.projectDir}/dokka"

    clean {
        delete(dokkaOutputDir)
    }

    dokkaHtml.configure {
        dependsOn(clean)
        outputDirectory.set(file(dokkaOutputDir))
    }

}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    doFirst { require(!Library.isUndefined) { "No release/snapshot version found." } }
}

publishing {
    publications {
        create<MavenPublication>(Library.name) {
            from(components["kotlin"])
            groupId = Library.group
            artifactId = Library.name
            version = Library.version

            artifact(sourcesJar.get())

            pom {
                name.set(Library.name)
                description.set(Library.description)
                url.set(Library.url)

                organization {
                    name.set(Organization.name)
                    url.set(Organization.url)
                }

                developers {
                    developer {
                        name.set(Developer.name)
                    }
                }

                issueManagement {
                    system.set(Issue.system)
                    url.set(Issue.url)
                }

                licenses {
                    license {
                        name.set(License.name)
                        url.set(License.url)
                    }
                }
                scm {
                    connection.set(SCM.connection)
                    developerConnection.set(SCM.developerConnection)
                    url.set(Library.url)
                }
            }

            if (!isJitPack) {
                repositories {
                    maven {
                        url = if (Library.isSnapshot) uri(Repository.snapshotsUrl) else uri(Repository.releasesUrl)
                        credentials {
                            username = Repository.username
                            password = Repository.password
                        }
                    }
                }
            }

        }
    }

}

if (!isJitPack && Library.isRelease) {
    signing {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign(publishing.publications[Library.name])
    }
}