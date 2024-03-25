package app.softwork.kotlin.actions

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Input
import java.io.*

@CacheableTask
abstract class CreateCustomWebpackConfig : DefaultTask() {
    @get:Input
    abstract val nodeVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val entry: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(project.layout.buildDirectory.dir("actions/webpack"))
    }

    @TaskAction
    fun write() {
        val file = File(outputDir.asFile.get(), "webpack.node.js")
        val unused = """
            import path from "path";
            import { fileURLToPath } from "url";

            const __dirname = path.dirname(fileURLToPath(import.meta.url));    
                
            config.entry = {
              main: [path.resolve(__dirname, "${entry.asFile.get().toRelativeString(entry.asFile.get().parentFile.parentFile)}")]
            };
        """.trimIndent()

        file.writeText(
            """ 
            config.experiments = {
              outputModule: true,
            };
            
            config.target = 'node${nodeVersion.get()}';
            
            config.output = {
              filename: config.output.filename,
              path: config.output.path,
              
              library: {
                type: "module",
              },
              module: true,
              chunkFormat: 'module',
            };

        """.trimIndent()
        )
    }
}
