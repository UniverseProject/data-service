plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("net.researchgate.release") version "3.0.0"
    `maven-publish`
    signing
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "plugin.serialization")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

val kotlinVersion: String by project
val kotlinCoroutineReactiveVersion: String by project
val ktorVersion: String by project
val ktSerializationVersion: String by project
val exposedVersion: String by project
val cacheVersion: String by project
val loggingVersion: String by project
val slf4jVersion: String by project
val mockkVersion: String by project
val junitVersion: String by project
val testContainersVersion: String by project
val psqlVersion: String by project
val lettuceVersion: String by project
val kotlinMojangApi: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit5"))
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutineReactiveVersion")

    implementation("io.github.universeproject:kotlin-mojang-api-jvm:$kotlinMojangApi")

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

    // Logging information
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    // Create fake instance (mock) of components for tests
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Junit to run tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
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
        kotlinOptions.jvmTarget = "1.8"
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

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("REPOSITORY_USERNAME"))
            password.set(System.getenv("REPOSITORY_PASSWORD"))
        }
    }
}

configure(allprojects) {
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
    if(signingKey != null && signingPassword != null) {
        signing {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
    }

    publishing {
        val dokkaOutputDir = "$buildDir/dokka/${this@configure.name}"

        tasks.dokkaHtml {
            outputDirectory.set(file(dokkaOutputDir))
        }

        val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
            delete(dokkaOutputDir)
        }

        val javadocJar = tasks.register<Jar>("javadocJar") {
            dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaOutputDir)
        }

        publications {
            val projectGitUrl = "https://github.com/UniverseProject/data-service"
            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set(this@configure.name)
                    description.set(project.description)
                    url.set(projectGitUrl)

                    issueManagement {
                        system.set("GitHub")
                        url.set("$projectGitUrl/issues")
                    }

                    ciManagement {
                        system.set("GitHub Actions")
                    }

                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://mit-license.org/")
                        }
                    }

                    developers {
                        developer {
                            name.set("Distractic")
                            email.set("Distractic@outlook.fr")
                            url.set("https://github.com/Distractic")
                        }
                    }

                    scm {
                        connection.set("scm:git:$projectGitUrl.git")
                        developerConnection.set("scm:git:git@github.com:UniverseProject/data-service.git")
                        url.set(projectGitUrl)
                    }

                    distributionManagement {
                        downloadUrl.set("$projectGitUrl/releases")
                    }
                }
            }
        }
    }
}

release {
    tagTemplate.set("v${version}")
}