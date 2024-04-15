package com.github.actions

@JsModule("@actions/core")
external val core: Core

external interface Core {
    fun getInput(name: String, options: InputOptions = definedExternally): String
    fun getBooleanInput(name: String, options: InputOptions = definedExternally): Boolean
    fun setOutput(name: String, value: Any)
    fun exportVariable(name: String, value: Any)
    fun isDebug(): Boolean
}

external interface InputOptions {
    var required: Boolean?
    var trimWhitespace: Boolean?
}

fun InputOptions(required: Boolean? = null, trimWhitespace: Boolean? = null): InputOptions {
    val obj = js("{}").unsafeCast<InputOptions>()
    obj.required = required
    obj.trimWhitespace = trimWhitespace
    return obj
}
