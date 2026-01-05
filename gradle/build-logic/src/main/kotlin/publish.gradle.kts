import dev.sigstore.sign.tasks.SigstoreSignFilesTask
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    id("maven-publish")
    id("signing")
    id("io.github.hfhbd.mavencentral")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("app.softwork Kotlin Actions")
            description.set("Write GitHub Actions in Kotlin")
            url.set("https://github.com/hfhbd/kotlin-actions")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("hfhbd")
                    name.set("Philip Wedemann")
                    email.set("mybztg+mavencentral@icloud.com")
                }
            }
            scm {
                connection.set("scm:git://github.com/hfhbd/kotlin-actions.git")
                developerConnection.set("scm:git://github.com/hfhbd/kotlin-actions.git")
                url.set("https://github.com/hfhbd/kotlin-actions")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull,
    )
    isRequired = providers.gradleProperty("signingKey").isPresent
    sign(publishing.publications)
}

// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

// https://github.com/sigstore/sigstore-java/issues/1146
tasks.withType<SigstoreSignFilesTask>().configureEach {
    launcher.set(serviceOf<JavaToolchainService>().launcherFor { })
}
