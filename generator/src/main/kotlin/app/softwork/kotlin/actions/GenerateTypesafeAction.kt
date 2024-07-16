package app.softwork.kotlin.actions

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.*
import java.io.*

private fun String.toCamelCase(): String = replace("-(.)".toRegex()) {
    it.groups[1]!!.value.replaceFirstChar { it.uppercaseChar() }
}

public fun File.generateCode(outputFile: File) {
    bufferedReader().generateCode().writeTo(outputFile)
}

internal fun BufferedReader.generateCode(): FileSpec {
    val jsonText = readText()
    val actionJson = jsonText.replace(replaceActionExpressions, "\"\"")
    return json.decodeFromString<ActionYml>(actionJson).generateCode()
}

private val getInput = MemberName("actions.core", "getInput", isExtension = true)
private val setFailed = MemberName("actions.core", "setFailed", isExtension = true)
private val setOutput = MemberName("actions.core", "setOutput", isExtension = true)
private val InputOptions = MemberName("actions.core", "InputOptions", isExtension = true)

internal fun ActionYml.generateCode(): FileSpec {
    val builder = FileSpec.builder("", "action")

    val outputClass = if (outputs.isNotEmpty()) {
        TypeSpec.classBuilder("Outputs").apply {
            addModifiers(KModifier.DATA)
            val constructor = FunSpec.constructorBuilder()
            for ((name, output) in outputs) {
                addProperty(
                    PropertySpec.builder(name.toCamelCase(), STRING).addKdoc(output.description)
                        .initializer(name.toCamelCase()).build()
                )
                constructor.addParameter(name.toCamelCase(), STRING)
            }
            primaryConstructor(constructor.build())
        }.build()
    } else null

    builder.addFunction(FunSpec.builder("main").apply {
        addModifiers(KModifier.SUSPEND)

        beginControlFlow("try")

        val functionInputs = CodeBlock.builder()
        val nameAllocator = NameAllocator()
        for ((name, input) in inputs) {
            val kotlinName = name.toCamelCase()
            val options = if (input.required) {
                CodeBlock.of(", %M(required = true)", InputOptions)
            } else CodeBlock.of("")
            functionInputs.add(
                "\n  %L = %M(%S%L)%L,", nameAllocator.newName(kotlinName), getInput, name, options,
                if (!input.required) CodeBlock.of(".ifEmpty { null }") else CodeBlock.of("")
            )
        }

        if (outputs.isEmpty()) {
            addCode("action(%L", functionInputs.build())
            if (inputs.isNotEmpty()) {
                addCode("\n")
            }
            addCode(")\n")
        } else {
            addCode("val outputs: %N = action(%L", outputClass!!, functionInputs.build())
            if (inputs.isNotEmpty()) {
                addCode("\n")
            }
            addCode(")\n")
            val outputNames = NameAllocator()
            for ((name) in outputs) {
                addCode(
                    "\n%M(%S, outputs.%L)", setOutput, name, outputNames.newName(name.toCamelCase())
                )
            }
            addCode("\n")
        }
        nextControlFlow("catch (e: %T)", ClassName("kotlin", "Throwable"))
        addStatement("setFailed(e)")
        endControlFlow()
    }.build())
    if (outputClass != null) {
        builder.addType(outputClass)
    }

    builder.addFunction(FunSpec.builder("setFailed").apply {
        addAnnotation(AnnotationSpec.builder(ClassName("kotlin.js", "JsModule")).apply {
            addMember("%S", "@actions/core")
        }.build())
        addKdoc("https://github.com/JetBrains/kotlin-wrappers/issues/2298")
        addModifiers(KModifier.EXTERNAL)
        addParameter("error", ClassName("kotlin", "Throwable"))
    }.build())

    return builder.build()
}
