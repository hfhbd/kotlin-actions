import app.softwork.kotlin.actions.GenerateTypesafeAction
import app.softwork.kotlin.actions.VERSION

val workerActionDeps = configurations.dependencyScope("kotlinActions")

dependencies {
    workerActionDeps("app.softwork.kotlin.actions", "generator", VERSION)
}

val kotlinActionsWorkerClasspath = configurations.resolvable("kotlinActionsWorkerClasspath") {
    extendsFrom(workerActionDeps.get())
}

val rawActionFile = layout.projectDirectory.file("action.yml")

tasks.register("generateTypesafeAction", GenerateTypesafeAction::class) {
    workerClasspath.from(kotlinActionsWorkerClasspath)
    actionFile.convention(rawActionFile)
    outputDirectory.convention(layout.buildDirectory.dir("actions/generated"))
}
