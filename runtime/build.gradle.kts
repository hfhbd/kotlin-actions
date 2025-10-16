plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    js {
        nodejs()
        useEsModules()

        compilerOptions {
            target.set("es2015")
        }
    }

    sourceSets {
        jsMain {
            dependencies {
                api(libs.coroutines.core)
                api(kotlinWrappers.actions.toolkit)
            }
        }
        jsTest {
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
