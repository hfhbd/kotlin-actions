import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateTypesafeAction : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val actionFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        actionFile.convention(project.layout.projectDirectory.file("action.yml"))
        outputDirectory.convention(project.layout.buildDirectory.dir("actions/generated"))
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }
    }

    @TaskAction
    fun generate() {
        val actionFile = actionFile.get().asFile
        val actionDefinition = json.decodeFromString<ActionYml>(actionFile.readText())

        actionDefinition.generateCode().writeTo(outputDirectory.get().asFile)
    }
}

private fun String.toCamelCase(): String = replace("-(.)".toRegex()) {
    it.groups[1]!!.value.replaceFirstChar { it.uppercaseChar() }
}

internal fun ActionYml.generateCode(): FileSpec {
    val builder = FileSpec.builder("", "action")
    builder.addFunction(FunSpec.builder("main").apply {
        addModifiers(KModifier.SUSPEND)

        val functionInputs = CodeBlock.builder()
        val nameAllocator = NameAllocator()
        if (inputs != null) {
            for ((name, input) in inputs) {
                val kotlinName = name.toCamelCase()
                val options = if (input.required) {
                    CodeBlock.builder()
                        .beginControlFlow("")
                        .add("required = true\n")
                        .unindent()
                        .add("}")
                        .build()
                } else CodeBlock.of("")
                functionInputs.add("\n%L = core.getInput(%S)%L,", nameAllocator.newName(kotlinName), name, options)
            }
        }

        addCode("val outputs = action(%L", functionInputs.build())
        if (outputs != null) {
            val outputNames = NameAllocator()
            for ((name) in outputs) {
                val kotlinName = name.toCamelCase()
                addStatement("core.setOutput(%S, outputs.%M)", name, MemberName("", outputNames.newName(kotlinName)))
                addCode("\n")
            }
        }
        addCode(")")
    }.build())
    return builder.build()
}
