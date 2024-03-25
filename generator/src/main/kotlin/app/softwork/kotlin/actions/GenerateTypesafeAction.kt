package app.softwork.kotlin.actions

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.*
import java.io.*

private fun String.toCamelCase(): String = replace("-(.)".toRegex()) {
    it.groups[1]!!.value.replaceFirstChar { it.uppercaseChar() }
}

private val json = Json {
    ignoreUnknownKeys = true
}

public fun File.generateCode(outputFile: File) {
    bufferedReader().generateCode().writeTo(outputFile)
}

internal fun BufferedReader.generateCode(): FileSpec {
    return json.decodeFromString<ActionYml>(readText()).generateCode()
}

private val core = MemberName("com.github.actions", "core", isExtension = true)

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
                        .add("  required = true\n")
                        .unindent()
                        .add("  }")
                        .build()
                } else CodeBlock.of("")
                functionInputs.add(
                    "\n  %L = %M.getInput(%S)%L,", nameAllocator.newName(kotlinName),
                    core,
                    name, options
                )
            }
        }

        if (outputs == null) {
            addCode("action(%L", functionInputs.build())
        } else {
            addCode("val outputs = action(%L", functionInputs.build())
            if(inputs != null) {
                addCode("\n")
            }
            addCode(")\n")
            val outputNames = NameAllocator()
            for ((name) in outputs) {
                val kotlinName = name.toCamelCase()
                addStatement(
                    "%M.setOutput(%S, outputs.%M)",
                    core,
                    name,
                    MemberName("", outputNames.newName(kotlinName))
                )
                addCode("\n")
            }
        }
    }.build())
    return builder.build()
}
