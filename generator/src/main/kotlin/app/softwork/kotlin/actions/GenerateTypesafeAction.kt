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

    val outputClass = if (outputs != null) {
        TypeSpec.classBuilder("Outputs").apply {
            addModifiers(KModifier.DATA)
            val constructor = FunSpec.constructorBuilder()
            for ((name, output) in outputs) {
                addProperty(
                    PropertySpec.builder(name.toCamelCase(), STRING).addKdoc(output.description).initializer(name.toCamelCase()).build()
                )
                constructor.addParameter(name.toCamelCase(), STRING)
            }
            primaryConstructor(constructor.build())
        }.build()
    } else null

    builder.addFunction(FunSpec.builder("main").apply {
        addModifiers(KModifier.SUSPEND)

        val functionInputs = CodeBlock.builder()
        val nameAllocator = NameAllocator()
        if (inputs != null) {
            for ((name, input) in inputs) {
                val kotlinName = name.toCamelCase()
                val options = if (input.required) {
                    CodeBlock.builder().beginControlFlow("").add("  required = true\n").unindent().add("  }").build()
                } else CodeBlock.of("")
                functionInputs.add(
                    "\n  %L = %M.getInput(%S)%L,", nameAllocator.newName(kotlinName), core, name, options
                )
            }
        }

        if (outputs == null) {
            addCode("action(%L", functionInputs.build())
            if (inputs != null) {
                addCode("\n")
            }
            addCode(")\n")
        } else {
            addCode("val outputs: %N = action(%L", outputClass!!, functionInputs.build())
            if (inputs != null) {
                addCode("\n")
            }
            addCode(")\n")
            val outputNames = NameAllocator()
            for ((name) in outputs) {
                val kotlinName = name.toCamelCase()
                addStatement(
                    "%M.setOutput(%S, outputs.%L)", core, name, outputNames.newName(name.toCamelCase())
                )
                addCode("\n")
            }
        }
    }.build())
    if (outputClass != null) {
        builder.addType(outputClass)
    }
    return builder.build()
}
