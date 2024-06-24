plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    js {
        nodejs()

        compilerOptions {
            target.set("es2015")
        }
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                api(libs.coroutines.core)
                api(npm("@actions/github", "6.0.0"))
                api("org.jetbrains.kotlin-wrappers:kotlin-actions-toolkit:0.0.1-pre.761")
            }
        }
        named("jsTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

val emptyJar by tasks.registering(Jar::class)

publishing {
    publications.withType(MavenPublication::class).configureEach {
        artifact(emptyJar) {
            classifier = "javadoc"
        }
    }
}
